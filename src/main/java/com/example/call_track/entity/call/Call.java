package com.example.call_track.entity.call;

import com.example.call_track.entity.PhoneNumber;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "calls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Call {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime callDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_phone_id", nullable = true)
    @ToString.Exclude
    private PhoneNumber callerPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "callee_phone_id", nullable = true)
    @ToString.Exclude
    private PhoneNumber calleePhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false)
    private CallType callType;

    @Column(name = "duration_seconds", nullable = false)
    private long durationSeconds;

    @Column(name = "price_per_minute", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerMinute;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
