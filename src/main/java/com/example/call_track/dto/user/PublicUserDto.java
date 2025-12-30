package com.example.call_track.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicUserDto {
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
    private String avatarPath;
    private String publicContactInfo;
}