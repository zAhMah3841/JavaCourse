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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneNumberService phoneNumberService;
    private final AvatarService avatarService;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
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

        userRepository.save(currentUser);
    }

    @Transactional
    public void updateAvatar(MultipartFile file) {
        User currentUser = getCurrentAuthenticatedUser();

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Delete old avatar if exists
        avatarService.deleteAvatar(currentUser.getAvatarPath());

        // Save new avatar
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path avatarPath = Paths.get("uploads", "avatars", fileName);
            Files.createDirectories(avatarPath.getParent());
            Files.copy(file.getInputStream(), avatarPath, StandardCopyOption.REPLACE_EXISTING);

            currentUser.setAvatarPath("avatars/" + fileName);
            userRepository.save(currentUser);
        } catch (IOException e) {
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
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public String initiatePasswordReset(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        return code;
    }

    public boolean verifyResetCode(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        return  user.getResetCode() != null && user.getResetCode().equals(code) &&
                user.getResetCodeExpiry().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        if (!verifyResetCode(resetPasswordDto.getUsername(), resetPasswordDto.getCode()))
            throw new IllegalArgumentException("Invalid or expired reset code");

        User user = userRepository.findByUsername(resetPasswordDto.getUsername()).get();
        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        userRepository.save(user);
    }

    public List<User> findAll() { return userRepository.findAll(); }
    public User save(User user) { return userRepository.save(user); }

    public Optional<User> findById(UUID id) { return userRepository.findById(id); }
    public boolean existsByUsername(String username) { return userRepository.existsByUsername(username); }
}
