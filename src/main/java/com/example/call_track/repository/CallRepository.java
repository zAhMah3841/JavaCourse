package com.example.call_track.repository;

import com.example.call_track.entity.call.Call;
import com.example.call_track.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CallRepository extends JpaRepository<Call, UUID> {
    List<Call> findByCaller(User caller);
    List<Call> findByCallee(User callee);
    List<Call> findByCallerOrCallee(User caller, User callee);
    List<Call> findByCallDateTimeBetween(LocalDateTime start, LocalDateTime end);
}