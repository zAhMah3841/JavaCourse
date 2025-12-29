package com.example.call_track.service;

import com.example.call_track.entity.call.Call;
import com.example.call_track.entity.call.CallType;
import com.example.call_track.entity.user.User;
import com.example.call_track.repository.CallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CallService {
    private final CallRepository callRepository;

    public Call saveCall(Call call) {
        return callRepository.save(call);
    }

    public Optional<Call> findById(UUID id) {
        return callRepository.findById(id);
    }

    public List<Call> findAll() {
        return callRepository.findAll();
    }

    public List<Call> findByCaller(User caller) {
        return callRepository.findByCaller(caller);
    }

    public List<Call> findByCallee(User callee) {
        return callRepository.findByCallee(callee);
    }

    public List<Call> findByCallerOrCallee(User caller, User callee) {
        return callRepository.findByCallerOrCallee(caller, callee);
    }

    public List<Call> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return callRepository.findByCallDateTimeBetween(start, end);
    }

    public Call createCall(User caller, User callee, CallType callType, long durationSeconds, BigDecimal pricePerMinute) {
        BigDecimal totalCost = calculateTotalCost(durationSeconds, pricePerMinute);

        Call call = Call.builder()
                .callDateTime(LocalDateTime.now())
                .caller(caller)
                .callee(callee)
                .callType(callType)
                .durationSeconds(durationSeconds)
                .pricePerMinute(pricePerMinute)
                .totalCost(totalCost)
                .build();

        return saveCall(call);
    }

    private BigDecimal calculateTotalCost(long durationSeconds, BigDecimal pricePerMinute) {
        BigDecimal durationMinutes = BigDecimal.valueOf(durationSeconds).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_UP);
        return durationMinutes.multiply(pricePerMinute);
    }
}
