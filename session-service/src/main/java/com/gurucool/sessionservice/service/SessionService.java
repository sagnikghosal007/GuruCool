package com.gurucool.sessionservice.service;

import com.gurucool.common.dto.PagedResponse;
import com.gurucool.common.event.*;
import com.gurucool.common.exception.DuplicateResourceException;
import com.gurucool.common.exception.ResourceNotFoundException;
import com.gurucool.common.exception.UnauthorizedException;
import com.gurucool.sessionservice.dto.*;
import com.gurucool.sessionservice.entity.*;
import com.gurucool.sessionservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String IDEMPOTENCY_PREFIX = "idempotency:booking:";

    private final SessionRepository sessionRepository;
    private final SessionBookingRepository bookingRepository;
    private final WaitlistRepository waitlistRepository;
    private final SessionRecordingRepository recordingRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public SessionResponse createSession(UUID mentorId, CreateSessionRequest request) {
        Instant minScheduledAt = Instant.now().plus(Duration.ofHours(1));
        if (request.scheduledAt().isBefore(minScheduledAt)) {
            throw new IllegalArgumentException("Session must be scheduled at least 1 hour in the future");
        }

        if (request.isPaid() && (request.priceAmount() == null || request.priceAmount().signum() <= 0)) {
            throw new IllegalArgumentException("Paid sessions require a positive price amount");
        }

        Session session = Session.builder()
                .mentorId(mentorId)
                .title(request.title())
                .description(request.description())
                .sessionType(SessionType.valueOf(request.sessionType()))
                .scheduledAt(request.scheduledAt())
                .durationMinutes(request.durationMinutes())
                .maxParticipants(request.maxParticipants())
                .currentParticipants(0)
                .status(SessionStatus.UPCOMING)
                .meetingLink(request.meetingLink())
                .isPaid(request.isPaid())
                .priceAmount(request.priceAmount() != null ? request.priceAmount() : java.math.BigDecimal.ZERO)
                .priceCurrency(request.priceCurrency() != null ? request.priceCurrency() : "INR")
                .build();

        session = sessionRepository.save(session);
        log.info("Session created: id={}, mentorId={}", session.getId(), mentorId);
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SessionResponse> listSessions(UUID mentorId, String status, Boolean isPaid,
                                                        Instant fromDate, Instant toDate, Pageable pageable) {
        SessionStatus sessionStatus = status != null ? SessionStatus.valueOf(status) : null;
        Page<Session> page = sessionRepository.findWithFilters(mentorId, sessionStatus, isPaid, fromDate, toDate, pageable);
        return PagedResponse.from(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(UUID sessionId) {
        return toResponse(sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId)));
    }

    @Transactional
    public SessionResponse updateSession(UUID sessionId, UUID mentorId, CreateSessionRequest request) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
        if (!session.getMentorId().equals(mentorId))
            throw new UnauthorizedException("You can only update your own sessions");

        session.setTitle(request.title());
        session.setDescription(request.description());
        session.setScheduledAt(request.scheduledAt());
        session.setDurationMinutes(request.durationMinutes());
        session.setMaxParticipants(request.maxParticipants());
        session.setMeetingLink(request.meetingLink());
        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public void cancelSession(UUID sessionId, UUID userId, String role, String reason) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        boolean isOwner = session.getMentorId().equals(userId);
        boolean isAdmin = "PLATFORM_ADMIN".equals(role) || "COLLEGE_ADMIN".equals(role);
        if (!isOwner && !isAdmin) throw new UnauthorizedException("Not authorized to cancel this session");

        session.setStatus(SessionStatus.CANCELLED);
        session.setCancellationReason(reason);
        sessionRepository.save(session);

        List<UUID> bookedStudents = bookingRepository.findActiveBookingsBySessionId(sessionId)
                .stream().map(SessionBooking::getStudentId).collect(Collectors.toList());

        if (!bookedStudents.isEmpty()) {
            kafkaProducerService.publishSessionCancelled(new SessionCancelledEvent(
                    sessionId, session.getTitle(), bookedStudents, session.getMentorId(), null, reason));
        }
        log.info("Session cancelled: id={}, affectedStudents={}", sessionId, bookedStudents.size());
    }

    @Transactional
    public BookingResponse bookSession(UUID sessionId, UUID studentId, String idempotencyKey) {
        // Idempotency check — Redis first
        String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            log.debug("Returning cached booking response for idempotencyKey={}", idempotencyKey);
        }

        // DB check
        if (bookingRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            SessionBooking existing = bookingRepository.findByIdempotencyKey(idempotencyKey).get();
            return new BookingResponse(existing.getId(), existing.getSessionId(), existing.getStudentId(),
                    existing.getStatus().name(), false, null, existing.getBookedAt());
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        if (session.getStatus() != SessionStatus.UPCOMING) {
            throw new IllegalStateException("Session is not available for booking");
        }

        // Waitlist
        if (session.getCurrentParticipants() >= session.getMaxParticipants()) {
            if (waitlistRepository.existsBySessionIdAndStudentId(sessionId, studentId)) {
                throw new DuplicateResourceException("Already on waitlist for this session");
            }
            long position = waitlistRepository.countBySessionId(sessionId) + 1;
            Waitlist waitlist = Waitlist.builder()
                    .sessionId(sessionId)
                    .studentId(studentId)
                    .joinedAt(Instant.now())
                    .position((int) position)
                    .build();
            waitlistRepository.save(waitlist);
            return new BookingResponse(null, sessionId, studentId, "WAITLISTED", false, (int) position, Instant.now());
        }

        BookingStatus initialStatus = session.getIsPaid() ? BookingStatus.PENDING : BookingStatus.CONFIRMED;

        SessionBooking booking = SessionBooking.builder()
                .sessionId(sessionId)
                .studentId(studentId)
                .status(initialStatus)
                .bookedAt(Instant.now())
                .idempotencyKey(idempotencyKey)
                .build();
        booking = bookingRepository.save(booking);

        if (initialStatus == BookingStatus.CONFIRMED) {
            session.setCurrentParticipants(session.getCurrentParticipants() + 1);
            sessionRepository.save(session);
        }

        // Cache idempotency
        redisTemplate.opsForValue().set(redisKey, booking.getId().toString(), Duration.ofMinutes(5));

        kafkaProducerService.publishSessionBooked(new SessionBookedEvent(
                booking.getId(), sessionId, session.getTitle(), studentId, null, null,
                session.getMentorId(), null, null, session.getIsPaid(), session.getPriceAmount(),
                session.getScheduledAt().toString()));

        log.info("Session booked: bookingId={}, sessionId={}, studentId={}, status={}",
                booking.getId(), sessionId, studentId, initialStatus);

        return new BookingResponse(booking.getId(), sessionId, studentId, initialStatus.name(),
                session.getIsPaid(), null, booking.getBookedAt());
    }

    @Transactional
    public void cancelBooking(UUID sessionId, UUID bookingId, UUID studentId) {
        SessionBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getStudentId().equals(studentId)) throw new UnauthorizedException("Not your booking");

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        bookingRepository.save(booking);

        if (session.getCurrentParticipants() > 0) {
            session.setCurrentParticipants(session.getCurrentParticipants() - 1);
            sessionRepository.save(session);
        }

        // Refund if paid
        if (session.getIsPaid() && booking.getPaymentId() != null) {
            kafkaProducerService.publishRefundRequested(new PaymentRefundRequestedEvent(
                    bookingId, studentId, session.getPriceAmount(), "Student cancelled booking"));
        }

        // Promote from waitlist
        waitlistRepository.findFirstBySessionIdOrderByPosition(sessionId).ifPresent(waitlisted -> {
            String newIdempotencyKey = UUID.randomUUID().toString();
            SessionBooking promoted = SessionBooking.builder()
                    .sessionId(sessionId)
                    .studentId(waitlisted.getStudentId())
                    .status(session.getIsPaid() ? BookingStatus.PENDING : BookingStatus.CONFIRMED)
                    .bookedAt(Instant.now())
                    .idempotencyKey(newIdempotencyKey)
                    .build();
            promoted = bookingRepository.save(promoted);
            waitlistRepository.delete(waitlisted);

            kafkaProducerService.publishWaitlistPromoted(new WaitlistPromotedEvent(
                    sessionId, session.getTitle(), waitlisted.getStudentId(), null, promoted.getId()));
        });
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getStudentBookings(UUID studentId, Pageable pageable) {
        Page<SessionBooking> page = bookingRepository.findByStudentId(studentId, pageable);
        return PagedResponse.from(page, b -> new BookingResponse(b.getId(), b.getSessionId(),
                b.getStudentId(), b.getStatus().name(), false, null, b.getBookedAt()));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SessionResponse> getMentorSessions(UUID mentorId, Pageable pageable) {
        Page<Session> page = sessionRepository.findByMentorId(mentorId, pageable);
        return PagedResponse.from(page, this::toResponse);
    }

    @Transactional
    public void updateSessionStatus(UUID sessionId, UUID mentorId, String newStatus) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        if (!session.getMentorId().equals(mentorId)) throw new UnauthorizedException("Not your session");

        SessionStatus target = SessionStatus.valueOf(newStatus);
        session.setStatus(target);
        sessionRepository.save(session);

        if (target == SessionStatus.COMPLETED) {
            List<UUID> attendedStudents = bookingRepository.findBySessionIdAndStatus(sessionId, BookingStatus.CONFIRMED)
                    .stream().map(SessionBooking::getStudentId).collect(Collectors.toList());
            kafkaProducerService.publishSessionCompleted(new SessionCompletedEvent(
                    sessionId, session.getTitle(), mentorId, attendedStudents));
        }
    }

    @Transactional
    public void addRecording(UUID sessionId, UUID mentorId, String recordingUrl, Integer durationSeconds) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
        if (!session.getMentorId().equals(mentorId)) throw new UnauthorizedException("Not your session");

        SessionRecording recording = SessionRecording.builder()
                .sessionId(sessionId)
                .recordingUrl(recordingUrl)
                .durationSeconds(durationSeconds)
                .uploadedAt(Instant.now())
                .build();
        recordingRepository.save(recording);
        log.info("Recording added for sessionId={}", sessionId);
    }

    // Payment completed consumer hook
    @Transactional
    public void confirmBookingOnPayment(UUID bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                sessionRepository.findById(booking.getSessionId()).ifPresent(session -> {
                    session.setCurrentParticipants(session.getCurrentParticipants() + 1);
                    sessionRepository.save(session);
                });
                log.info("Booking confirmed after payment: bookingId={}", bookingId);
            }
        });
    }

    private SessionResponse toResponse(Session s) {
        return new SessionResponse(s.getId(), s.getMentorId(), s.getTitle(), s.getDescription(),
                s.getSessionType().name(), s.getScheduledAt(), s.getDurationMinutes(),
                s.getMaxParticipants(), s.getCurrentParticipants(), s.getStatus().name(),
                s.getMeetingLink(), s.getIsPaid(), s.getPriceAmount(), s.getPriceCurrency(), s.getCreatedAt());
    }
}
