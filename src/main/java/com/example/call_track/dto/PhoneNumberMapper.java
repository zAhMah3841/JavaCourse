package com.example.call_track.dto;

import com.example.call_track.entity.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberMapper {

    public PhoneNumberDto toDto(PhoneNumber phoneNumber) {
        if (phoneNumber == null) return null;
        PhoneNumberDto dto = new PhoneNumberDto();
        dto.setId(phoneNumber.getId());
        dto.setPhone(phoneNumber.getPhone());
        dto.setPrimary(phoneNumber.isPrimary());
        dto.setCreatedAt(phoneNumber.getCreatedAt());
        dto.setUpdatedAt(phoneNumber.getUpdatedAt());
        return dto;
    }
}