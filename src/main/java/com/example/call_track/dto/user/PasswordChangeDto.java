package com.example.call_track.dto.user;

import com.example.call_track.utils.validation.PasswordMatches;
import com.example.call_track.utils.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@PasswordMatches
public class PasswordChangeDto {
    @NotBlank(message = "Current password must not be empty")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @ValidPassword(message = "Password does not meet security requirements")
    private String password;

    @NotBlank(message = "Password confirmation must not be empty")
    private String confirmPassword;
}
