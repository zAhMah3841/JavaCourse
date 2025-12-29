package com.example.call_track.dto.user;

import com.example.call_track.utils.validation.ValidPassword;
import com.example.call_track.utils.validation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@PasswordMatches(passwordField = "newPassword")
public class ResetPasswordDto {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "New password is required")
    @ValidPassword(message = "Password does not meet security requirements")
    private String newPassword;

    @NotBlank(message = "Password confirmation must not be empty")
    private String confirmPassword;
}
