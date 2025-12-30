package com.example.call_track.service;

import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.call.Call;
import com.example.call_track.entity.call.CallType;
import com.example.call_track.entity.user.User;
import com.example.call_track.repository.CallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public List<Call> findByCallerPhone(PhoneNumber callerPhone) {
        return callRepository.findByCallerPhone(callerPhone);
    }

    public List<Call> findByCalleePhone(PhoneNumber calleePhone) {
        return callRepository.findByCalleePhone(calleePhone);
    }

    public List<Call> findByCallerPhoneOrCalleePhone(PhoneNumber callerPhone, PhoneNumber calleePhone) {
        return callRepository.findByCallerPhoneOrCalleePhone(callerPhone, calleePhone);
    }

    public Page<Call> findByUser(User user, Pageable pageable) {
        return callRepository.findByUserWithPhones(user, pageable);
    }

    public Page<Call> findByUserWithFilters(User user, LocalDateTime startDate, LocalDateTime endDate,
                                            String callType, String phone, BigDecimal minCost, BigDecimal maxCost, Pageable pageable) {
        return callRepository.findByUserWithFilters(user, startDate, endDate, callType, phone, minCost, maxCost, pageable);
    }

    public List<Call> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return callRepository.findByCallDateTimeBetween(start, end);
    }

    public Call createCall(PhoneNumber callerPhone, PhoneNumber calleePhone, CallType callType, long durationSeconds, BigDecimal pricePerMinute) {
        BigDecimal totalCost = calculateTotalCost(durationSeconds, pricePerMinute);

        // Set call time to a random time in the past hour to spread out calls
        LocalDateTime callTime = LocalDateTime.now().minusSeconds((long) (Math.random() * 3600));

        Call call = Call.builder()
                .callDateTime(callTime)
                .callerPhone(callerPhone)
                .calleePhone(calleePhone)
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

    /**
     * Универсальный поиск вызовов пользователя с учетом всех возможных фильтров
     */
    public Page<Call> searchUserCalls(
            User user,
            String name, String myNumbers, String phone, String callType,
            String startDate, String endDate,
            String sortBy, String sortDir,
            BigDecimal minCost, BigDecimal maxCost,
            BigDecimal pricePerMinute, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable
    ) {
        return callRepository.findAll(
                com.example.call_track.spec.CallSpecifications.filterAll(
                        user, name, myNumbers, phone, callType, startDate, endDate, minCost, maxCost, pricePerMinute, minPrice, maxPrice
                ),
                org.springframework.data.domain.PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        com.example.call_track.spec.CallSpecifications.buildSort(sortBy, sortDir)
                )
        );
    }
}
