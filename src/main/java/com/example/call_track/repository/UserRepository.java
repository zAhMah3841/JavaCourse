package com.example.call_track.repository;

import com.example.call_track.entity.user.User;
import com.example.call_track.entity.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsernameActive(String username);

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleted = false")
    Optional<User> findByIdActive(UUID id);

    boolean existsByUsername(String username);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deleted = false")
    Long countByRoleActive(UserRole role);

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    Page<User> findAllActive(Pageable pageable);
}
