package com.example.call_track.service;

import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.user.User;
import com.example.call_track.repository.PhoneNumberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhoneNumberService {
    private final PhoneNumberRepository phoneNumberRepository;

    @Transactional
    public PhoneNumber addPhoneNumber(User user, String phone, boolean isPrimary) {
        if (phoneNumberRepository.existsByPhone(phone))
            throw new IllegalArgumentException("Phone number already exists");

        if (isPrimary) phoneNumberRepository.findByUser(user).forEach(pn -> {
            pn.setPrimary(false);
            phoneNumberRepository.save(pn);
        });

        PhoneNumber phoneNumber = PhoneNumber.builder()
                .user(user)
                .phone(phone)
                .isPrimary(isPrimary)
                .build();

        return phoneNumberRepository.save(phoneNumber);
    }

    @Transactional
    public void setPrimaryPhone(User user, UUID phoneNumberId) {
        PhoneNumber newPrimary = phoneNumberRepository.findById(phoneNumberId)
                .filter(pn -> pn.getUser().equals(user))
                .orElseThrow(() -> new IllegalArgumentException("Phone number not found or not owned by user"));

        phoneNumberRepository.findByUser(user).forEach(pn -> {
            pn.setPrimary(false);
            phoneNumberRepository.save(pn);
        });

        newPrimary.setPrimary(true);
        phoneNumberRepository.save(newPrimary);
    }

    @Transactional
    public void removePhoneNumber(User user, UUID phoneNumberId) {
        PhoneNumber phoneNumber = phoneNumberRepository.findById(phoneNumberId)
                .filter(pn -> pn.getUser().equals(user))
                .orElseThrow(() -> new IllegalArgumentException("Phone number not found or not owned by user"));

        List<PhoneNumber> userPhones = phoneNumberRepository.findByUser(user);
        if (userPhones.size() == 1)
            throw new IllegalArgumentException("Cannot remove the only phone number for the user.");

        if (phoneNumber.isPrimary() && userPhones.size() > 1)
            throw new IllegalArgumentException(
                    "Cannot remove primary phone number if others exist. Set another as primary first.");

        phoneNumberRepository.delete(phoneNumber);
    }

    public List<PhoneNumber> getPhoneNumbersForUser(User user) { return phoneNumberRepository.findByUser(user); }
    public Optional<PhoneNumber> getPrimaryPhoneForUser(User user) {
        return phoneNumberRepository.findByUserAndIsPrimaryTrue(user);
    }

    public Optional<PhoneNumber> findByPhone(String phone) { return phoneNumberRepository.findByPhone(phone); }

    public boolean existsByPhone(String phone) { return phoneNumberRepository.existsByPhone(phone); }
}
