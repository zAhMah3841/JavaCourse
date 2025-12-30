package com.example.call_track.controller;

import com.example.call_track.dto.PhoneNumberDto;
import com.example.call_track.dto.PhoneNumberMapper;
import com.example.call_track.dto.user.InitiateResetDto;
import com.example.call_track.dto.user.PasswordChangeDto;
import com.example.call_track.dto.user.ResetPasswordDto;
import com.example.call_track.dto.user.UpdateDto;
import com.example.call_track.dto.user.VerifyCodeDto;
import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.user.User;
import com.example.call_track.service.PhoneNumberService;
import com.example.call_track.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;
    private final PhoneNumberService phoneNumberService;
    private final PhoneNumberMapper phoneNumberMapper;

    @GetMapping("/profile")
    public ResponseEntity<UpdateDto> getProfile() {
        User user = userService.getCurrentAuthenticatedUser();
        UpdateDto updateDto = new UpdateDto();
        updateDto.setUsername(user.getUsername());
        updateDto.setFirstName(user.getFirstName());
        updateDto.setLastName(user.getLastName());
        updateDto.setMiddleName(user.getMiddleName());
        updateDto.setPublicContactInfo(user.getPublicContactInfo());
        return ResponseEntity.ok(updateDto);
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@Valid @RequestBody UpdateDto updateDto) {
        userService.updateUserProfile(updateDto);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @GetMapping("/phone-numbers")
    public ResponseEntity<List<PhoneNumberDto>> getPhoneNumbers() {
        User user = userService.getCurrentAuthenticatedUser();
        List<PhoneNumber> phoneNumbers = phoneNumberService.getPhoneNumbersForUser(user);
        List<PhoneNumberDto> phoneNumberDtos = phoneNumbers.stream()
                .map(phoneNumberMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(phoneNumberDtos);
    }

    @PostMapping("/phone-numbers")
    public ResponseEntity<PhoneNumberDto> addPhoneNumber(@Valid @RequestBody PhoneNumberDto dto) {
        User user = userService.getCurrentAuthenticatedUser();
        PhoneNumber phoneNumber = phoneNumberService.addPhoneNumber(user, dto.getPhone(), dto.isPrimary());
        return ResponseEntity.ok(phoneNumberMapper.toDto(phoneNumber));
    }

    @PutMapping("/phone-numbers/{id}/primary")
    public ResponseEntity<String> setPrimaryPhone(@PathVariable UUID id) {
        User user = userService.getCurrentAuthenticatedUser();
        phoneNumberService.setPrimaryPhone(user, id);
        return ResponseEntity.ok("Primary phone updated");
    }

    @DeleteMapping("/phone-numbers/{id}")
    public ResponseEntity<String> removePhoneNumber(@PathVariable UUID id) {
        try {
            User user = userService.getCurrentAuthenticatedUser();
            phoneNumberService.removePhoneNumber(user, id);
            return ResponseEntity.ok("Phone number removed");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordChangeDto passwordChangeDto) {
        userService.changePassword(passwordChangeDto);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        userService.updateAvatar(file);
        return ResponseEntity.ok("Avatar uploaded successfully");
    }

    //TODO: Fix that code
    @PostMapping("/initiate-reset")
    public ResponseEntity<Map<String, String>> initiateReset(@Valid @RequestBody InitiateResetDto dto) {
        try {
            String code = userService.initiatePasswordReset(dto.getUsername());
            Map<String, String> response = new HashMap<>();
            response.put("code", code);
            response.put("message", "This is a test function. In a real application, the code would be sent via email or SMS. Here is your reset code: " + code);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@Valid @RequestBody VerifyCodeDto dto) {
        try {
            if (userService.verifyResetCode(dto.getUsername(), dto.getCode())) {
                return ResponseEntity.ok("Code verified");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired reset code");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        try {
            userService.resetPassword(dto);
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
