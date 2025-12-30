package com.example.call_track.repository;

import com.example.call_track.entity.call.Call;
import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CallRepository extends JpaRepository<Call, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Call> {
    List<Call> findByCallerPhone(PhoneNumber callerPhone);
    List<Call> findByCalleePhone(PhoneNumber calleePhone);
    List<Call> findByCallerPhoneOrCalleePhone(PhoneNumber callerPhone, PhoneNumber calleePhone);
    List<Call> findByCallDateTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM Call c LEFT JOIN FETCH c.callerPhone cp LEFT JOIN FETCH cp.user LEFT JOIN FETCH c.calleePhone calp LEFT JOIN FETCH calp.user WHERE cp.user = :user OR calp.user = :user ORDER BY c.callDateTime DESC")
    Page<Call> findByUserWithPhones(User user, Pageable pageable);

    @Query("SELECT c FROM Call c LEFT JOIN c.callerPhone cp LEFT JOIN c.calleePhone calp " +
            "WHERE (cp.user = :user OR calp.user = :user) " +
            "AND (:startDate IS NULL OR c.callDateTime >= :startDate) " +
            "AND (:endDate IS NULL OR c.callDateTime <= :endDate) " +
            "AND (:callType IS NULL OR " +
            "(:callType = 'OUTGOING' AND cp.user = :user) OR " +
            "(:callType = 'INCOMING' AND calp.user = :user)) " +
            "AND (:phone IS NULL OR " +
            "((cp.user != :user AND cp.phone = :phone) OR (calp.user != :user AND calp.phone = :phone))) " +
            "AND (:minCost IS NULL OR c.totalCost >= :minCost) " +
            "AND (:maxCost IS NULL OR c.totalCost <= :maxCost) " +
            "ORDER BY c.callDateTime DESC")
    Page<Call> findByUserWithFilters(@Param("user") User user,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("callType") String callType,
                                     @Param("phone") String phone,
                                     @Param("minCost") BigDecimal minCost,
                                     @Param("maxCost") BigDecimal maxCost,
                                     Pageable pageable);
}