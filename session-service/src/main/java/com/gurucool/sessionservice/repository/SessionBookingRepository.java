package com.gurucool.sessionservice.repository;

import com.gurucool.sessionservice.entity.BookingStatus;
import com.gurucool.sessionservice.entity.SessionBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionBookingRepository extends JpaRepository<SessionBooking, UUID> {
    Optional<SessionBooking> findByIdempotencyKey(String idempotencyKey);
    Page<SessionBooking> findByStudentId(UUID studentId, Pageable pageable);
    List<SessionBooking> findBySessionIdAndStatus(UUID sessionId, BookingStatus status);
    boolean existsBySessionIdAndStudentId(UUID sessionId, UUID studentId);
    long countBySessionIdAndStatusNot(UUID sessionId, BookingStatus status);

    @Query("SELECT b FROM SessionBooking b WHERE b.sessionId = :sessionId AND b.status IN ('CONFIRMED','ATTENDED')")
    List<SessionBooking> findActiveBookingsBySessionId(UUID sessionId);
}
