package com.gurucool.userservice.repository;

import com.gurucool.userservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.userId = :userId")
    void revokeAllByUserId(UUID userId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.token = :token")
    void revokeByToken(String token);
}
