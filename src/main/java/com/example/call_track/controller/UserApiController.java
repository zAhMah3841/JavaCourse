package com.example.call_track.controller;

import com.example.call_track.dto.PhoneNumberDto;
import com.example.call_track.dto.PhoneNumberMapper;
import com.example.call_track.dto.user.InitiateResetDto;
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
        User user = userService.getCurrentAuthenticatedUser();
        phoneNumberService.removePhoneNumber(user, id);
        return ResponseEntity.ok("Phone number removed");
    }

    //TODO: Fix that code
    @PostMapping("/initiate-reset")
    public ResponseEntity<Map<String, String>> initiateReset(@Valid @RequestBody InitiateResetDto dto) {
        String code = userService.initiatePasswordReset(dto.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("code", code);
        response.put("message", "This is a test function. In a real application, the code would be sent via email or SMS. Here is your reset code: " + code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@Valid @RequestBody VerifyCodeDto dto) {
        if (userService.verifyResetCode(dto.getUsername(), dto.getCode())) {
            return ResponseEntity.ok("Code verified");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired reset code");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        userService.resetPassword(dto);
        return ResponseEntity.ok("Password reset successfully");
    }
}
