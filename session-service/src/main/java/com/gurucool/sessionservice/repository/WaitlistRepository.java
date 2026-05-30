package com.gurucool.sessionservice.repository;

import com.gurucool.sessionservice.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, UUID> {
    List<Waitlist> findBySessionIdOrderByPosition(UUID sessionId);
    Optional<Waitlist> findFirstBySessionIdOrderByPosition(UUID sessionId);
    boolean existsBySessionIdAndStudentId(UUID sessionId, UUID studentId);

    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.sessionId = :sessionId")
    long countBySessionId(UUID sessionId);
}
