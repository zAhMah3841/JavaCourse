package com.example.call_track.service;

import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.user.User;
import com.example.call_track.repository.CallRepository;
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
    private final CallRepository callRepository;

    @Transactional
    public PhoneNumber addPhoneNumber(User user, String phone, boolean isPrimary) {
        if (phoneNumberRepository.existsByPhone(phone))
            throw new IllegalArgumentException("Phone number already exists");

        List<PhoneNumber> userPhones = phoneNumberRepository.findByUser(user);
        boolean hasPrimary = userPhones.stream().anyMatch(PhoneNumber::isPrimary);

        if (isPrimary || !hasPrimary) {
            // If setting as primary or no primary exists, unset others
            userPhones.forEach(pn -> {
                pn.setPrimary(false);
                phoneNumberRepository.save(pn);
            });
            isPrimary = true;
        }

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

        // If removing primary, set another as primary
        if (phoneNumber.isPrimary() && userPhones.size() > 1) {
            Optional<PhoneNumber> another = userPhones.stream()
                    .filter(pn -> !pn.getId().equals(phoneNumberId))
                    .findFirst();
            if (another.isPresent()) {
                another.get().setPrimary(true);
                phoneNumberRepository.save(another.get());
            }
        }

        phoneNumberRepository.delete(phoneNumber);
    }

    public List<PhoneNumber> getPhoneNumbersForUser(User user) { return phoneNumberRepository.findByUser(user); }
    public Optional<PhoneNumber> getPrimaryPhoneForUser(User user) {
        return phoneNumberRepository.findByUserAndIsPrimaryTrue(user);
    }

    public Optional<PhoneNumber> findByPhone(String phone) { return phoneNumberRepository.findByPhone(phone); }

    public boolean existsByPhone(String phone) { return phoneNumberRepository.existsByPhone(phone); }
}
