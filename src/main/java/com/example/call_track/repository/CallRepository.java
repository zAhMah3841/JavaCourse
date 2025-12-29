package com.example.call_track.repository;

import com.example.call_track.entity.call.Call;
import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CallRepository extends JpaRepository<Call, UUID> {
    List<Call> findByCallerPhone(PhoneNumber callerPhone);
    List<Call> findByCalleePhone(PhoneNumber calleePhone);
    List<Call> findByCallerPhoneOrCalleePhone(PhoneNumber callerPhone, PhoneNumber calleePhone);
    List<Call> findByCallDateTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM Call c LEFT JOIN FETCH c.callerPhone cp LEFT JOIN FETCH cp.user LEFT JOIN FETCH c.calleePhone calp LEFT JOIN FETCH calp.user WHERE cp.user = :user OR calp.user = :user ORDER BY c.callDateTime DESC")
    List<Call> findByUserWithPhones(User user);
}