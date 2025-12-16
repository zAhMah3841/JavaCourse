package com.example.call_track.dto.user;

import com.example.call_track.utils.validation.PasswordMatches;
import com.example.call_track.utils.validation.ValidPassword;
import com.example.call_track.utils.validation.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@PasswordMatches
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDto {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Password is required")
    @ValidPassword(message = "Password does not meet security requirements")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Middle name must not exceed 50 characters")
    private String middleName;

    @ValidPhoneNumber(message = "Phone number format is invalid\n")
    @NotBlank(message = "Phone number is required")
    private String phone;
}