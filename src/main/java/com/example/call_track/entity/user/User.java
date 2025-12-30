package com.example.call_track.entity.user;

import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.utils.validation.ValidPhoneNumber;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = { "username" }) })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Username must not be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may contain only letters, digits, and underscores")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password must not be empty")
    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @NotBlank(message = "Last name must not be empty")
    @Size(max = 50, message = "Last name must not exceed 50 characters\n")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "First name must not be empty")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Size(max = 50, message = "Middle name must not exceed 50 characters")
    @Column(name = "middle_name")
    private String middleName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PhoneNumber> phoneNumbers;

    @Column(name = "avatar_path")
    private String avatarPath;

    @Column(name = "public_contact_info", length = 500)
    private String publicContactInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default private UserRole role = UserRole.USER;

    @CreatedDate private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;

    @Column(name = "force_password_change", nullable = false)
    @Builder.Default private boolean forcePasswordChange = false;

    @Column(name = "reset_code")
    private String resetCode;

    @Column(name = "reset_code_expiry")
    private LocalDateTime resetCodeExpiry;

    @Builder.Default private boolean accountNonExpired = true;
    @Builder.Default private boolean accountNonLocked = true;
    @Builder.Default private boolean credentialsNonExpired = true;
    @Builder.Default private boolean enabled = true;

    @Column(name = "deleted", nullable = false)
    @Builder.Default private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public boolean isAccountNonExpired() { return accountNonExpired; }
    @Override public boolean isAccountNonLocked() { return accountNonLocked; }
    @Override public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
    @Override public boolean isEnabled() { return enabled && !deleted; }

    public String getPrimaryPhone() {
        if (phoneNumbers == null) return "";
        return phoneNumbers.stream()
                .filter(PhoneNumber::isPrimary)
                .findFirst()
                .map(PhoneNumber::getPhone)
                .orElse("");
    }
}
