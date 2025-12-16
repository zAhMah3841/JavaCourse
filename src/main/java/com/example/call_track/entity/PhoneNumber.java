package com.example.call_track.entity;

import com.example.call_track.entity.user.User;
import com.example.call_track.utils.validation.ValidPhoneNumber;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "phone_numbers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"phone"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class PhoneNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ValidPhoneNumber(message = "Please provide a valid phone number")
    @NotBlank(message = "Phone number must not be empty")
    @Column(unique = true, nullable = false)
    private String phone;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}