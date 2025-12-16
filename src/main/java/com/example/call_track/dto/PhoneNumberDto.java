package com.example.call_track.dto;

import com.example.call_track.utils.validation.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PhoneNumberDto {
    private UUID id;

    @ValidPhoneNumber(message = "Phone number format is invalid")
    @NotBlank(message = "Phone number is required")
    private String phone;

    private boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}