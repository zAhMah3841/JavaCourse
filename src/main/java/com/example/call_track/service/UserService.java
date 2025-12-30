package com.example.call_track.service;

import com.example.call_track.dto.user.PasswordChangeDto;
import com.example.call_track.dto.user.RegistrationDto;
import com.example.call_track.dto.user.ResetPasswordDto;
import com.example.call_track.dto.user.UpdateDto;
import com.example.call_track.entity.user.User;
import com.example.call_track.entity.user.UserRole;
import com.example.call_track.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneNumberService phoneNumberService;
    private final AvatarService avatarService;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameActive(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Transactional
    public User registerUser(RegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername()))
            throw new IllegalArgumentException("Username already exists!");

        if (phoneNumberService.existsByPhone(registrationDto.getPhone()))
            throw new IllegalArgumentException("Phone number already registered!");

        String avatarPath = avatarService.generateAvatar(
                registrationDto.getFirstName(), registrationDto.getLastName());

        User user = User.builder()
                .username(registrationDto.getUsername())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .middleName(registrationDto.getMiddleName())
                .avatarPath(avatarPath)
                .role(UserRole.USER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        user = userRepository.save(user);
        phoneNumberService.addPhoneNumber(user, registrationDto.getPhone(), true);

        return user;
    }

    @Transactional
    public void updateUserProfile(UpdateDto updateDto) {
        User currentUser = getCurrentAuthenticatedUser();

        if (updateDto.getFirstName() != null && !updateDto.getFirstName().isBlank())
            currentUser.setFirstName(updateDto.getFirstName());

        if (updateDto.getLastName() != null && !updateDto.getLastName().isBlank())
            currentUser.setLastName(updateDto.getLastName());

        if (updateDto.getMiddleName() != null && !updateDto.getMiddleName().isBlank())
            currentUser.setMiddleName(updateDto.getMiddleName());

        if (updateDto.getUsername() != null && !updateDto.getUsername().isBlank()) {
            if (!currentUser.getUsername().equals(updateDto.getUsername())
                    && userRepository.existsByUsername(updateDto.getUsername()))
                throw new IllegalArgumentException("Username already exists!");

            currentUser.setUsername(updateDto.getUsername());
        }

        if (updateDto.getPublicContactInfo() != null)
            currentUser.setPublicContactInfo(updateDto.getPublicContactInfo());

        userRepository.save(currentUser);
    }

    @Transactional
    public void updateAvatar(MultipartFile file) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("Updating avatar for user {}", currentUser.getUsername());

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size too large. Maximum allowed size is 5MB");
        }

        // Check file type
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        boolean isImageByContentType = contentType != null && contentType.startsWith("image/");
        boolean isImageByExtension = originalFilename != null &&
                originalFilename.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$");

        if (!isImageByContentType && !isImageByExtension) {
            throw new IllegalArgumentException("Only image files are allowed (jpg, jpeg, png, gif, bmp, webp)");
        }

        // Delete old avatar if exists
        avatarService.deleteAvatar(currentUser.getAvatarPath());

        // Save new avatar
        try {
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            Path uploadDir = Paths.get("uploads").toAbsolutePath();
            Path avatarPath = uploadDir.resolve("avatars").resolve(fileName);
            Files.createDirectories(avatarPath.getParent());
            Files.copy(file.getInputStream(), avatarPath, StandardCopyOption.REPLACE_EXISTING);

            currentUser.setAvatarPath("avatars/" + fileName);
            userRepository.save(currentUser);
            logger.info("Avatar updated successfully for user {}", currentUser.getUsername());
        } catch (IOException e) {
            logger.error("Failed to save avatar for user {}", currentUser.getUsername(), e);
            throw new RuntimeException("Failed to save avatar", e);
        }
    }

    @Transactional
    public void changePassword(PasswordChangeDto passwordChangeDto) {
        User currentUser = getCurrentAuthenticatedUser();

        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), currentUser.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");

        if (passwordEncoder.matches(passwordChangeDto.getPassword(), currentUser.getPassword()))
            throw new IllegalArgumentException("New password must be different from current password");

        currentUser.setPassword(passwordEncoder.encode(passwordChangeDto.getPassword()));
        currentUser.setForcePasswordChange(false); // Reset the flag after password change
        userRepository.save(currentUser);
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated())
            throw new UsernameNotFoundException("User not authenticated");

        String username = authentication.getName();
        return userRepository.findByUsernameActive(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public String initiatePasswordReset(String username) {
        User user = userRepository.findByUsernameActive(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        return code;
    }

    public boolean verifyResetCode(String username, String code) {
        User user = userRepository.findByUsernameActive(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with username: " + username));

        return  user.getResetCode() != null && user.getResetCode().equals(code) &&
                user.getResetCodeExpiry().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        if (!verifyResetCode(resetPasswordDto.getUsername(), resetPasswordDto.getCode()))
            throw new IllegalArgumentException("Invalid or expired reset code");

        User user = userRepository.findByUsernameActive(resetPasswordDto.getUsername()).get();
        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        userRepository.save(user);
    }

    public List<User> findAll() { return userRepository.findAll(); }
    public List<User> findAllActive() { return userRepository.findAllActive(); }
    public User save(User user) { return userRepository.save(user); }

    public Optional<User> findById(UUID id) { return userRepository.findById(id); }
    public Optional<User> findByIdActive(UUID id) { return userRepository.findByIdActive(id); }
    public boolean existsByUsername(String username) { return userRepository.existsByUsername(username); }

    @Transactional
    public void deleteUser(UUID userId) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("deleteUser called by {} for userId {}", currentUser.getUsername(), userId);
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new IllegalArgumentException("Only admins can delete users");
        }

        User userToDelete = userRepository.findByIdActive(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userToDelete.getId().equals(currentUser.getId())) {
            // Allow deleting self only if there are other active admins
            long activeAdminCount = userRepository.countByRoleActive(UserRole.ADMIN);
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot delete the last admin");
            }
        }

        softDeleteUser(userToDelete);
        logger.info("User {} soft deleted", userToDelete.getUsername());
    }

    @Transactional
    public void deleteOwnAccount() {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("deleteOwnAccount called by {}", currentUser.getUsername());

        softDeleteUser(currentUser);
        logger.info("User {} soft deleted own account", currentUser.getUsername());
    }

    private void softDeleteUser(User user) {
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        // Optionally, soft delete phone numbers or leave them
        // For now, leave phone numbers as they are associated with the user
    }
}
