package com.example.call_track.repository;

import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, UUID> {
    Optional<PhoneNumber> findByPhone(String phoneNumber);
    Optional<PhoneNumber> findByUserAndIsPrimaryTrue(User user);
    List<PhoneNumber> findByUser(User user);

    boolean existsByPhone(String phoneNumber);
}