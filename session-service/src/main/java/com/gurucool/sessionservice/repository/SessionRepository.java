package com.gurucool.sessionservice.repository;

import com.gurucool.sessionservice.entity.Session;
import com.gurucool.sessionservice.entity.SessionStatus;
import com.gurucool.sessionservice.entity.SessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Page<Session> findByMentorId(UUID mentorId, Pageable pageable);
    Page<Session> findByStatus(SessionStatus status, Pageable pageable);
    Page<Session> findByMentorIdAndStatus(UUID mentorId, SessionStatus status, Pageable pageable);

    @Query("SELECT s FROM Session s WHERE (:mentorId IS NULL OR s.mentorId = :mentorId) " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:isPaid IS NULL OR s.isPaid = :isPaid) " +
           "AND (:fromDate IS NULL OR s.scheduledAt >= :fromDate) " +
           "AND (:toDate IS NULL OR s.scheduledAt <= :toDate)")
    Page<Session> findWithFilters(UUID mentorId, SessionStatus status, Boolean isPaid,
                                  Instant fromDate, Instant toDate, Pageable pageable);
}
