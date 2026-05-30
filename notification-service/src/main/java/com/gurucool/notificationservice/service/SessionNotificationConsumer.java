package com.gurucool.notificationservice.service;

import com.gurucool.common.constants.KafkaTopics;
import com.gurucool.common.event.SessionBookedEvent;
import com.gurucool.common.event.SessionCancelledEvent;
import com.gurucool.common.event.SessionCompletedEvent;
import com.gurucool.common.event.WaitlistPromotedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionNotificationConsumer {

    private final EmailService emailService;

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2), dltTopicSuffix = ".DLT")
    @KafkaListener(topics = KafkaTopics.SESSION_BOOKED, groupId = "notification-session-booked")
    public void handleSessionBooked(SessionBookedEvent event, Acknowledgment ack) {
        try {
            log.info("Processing session booked notification for bookingId={}", event.bookingId());
            if (event.studentEmail() != null) {
                emailService.sendTemplatedEmail(
                        event.studentEmail(),
                        "Session Booking Confirmed - " + event.sessionTitle(),
                        "session-booking-confirmation",
                        Map.of(
                            "studentName", event.studentName() != null ? event.studentName() : "Student",
                            "sessionTitle", event.sessionTitle(),
                            "scheduledAt", event.scheduledAt(),
                            "mentorName", event.mentorName() != null ? event.mentorName() : "Your Mentor",
                            "isPaid", event.isPaid(),
                            "amount", event.priceAmount()
                        )
                );
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing session.booked: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2), dltTopicSuffix = ".DLT")
    @KafkaListener(topics = KafkaTopics.SESSION_CANCELLED, groupId = "notification-session-cancelled")
    public void handleSessionCancelled(SessionCancelledEvent event, Acknowledgment ack) {
        try {
            log.info("Processing session cancellation notification for sessionId={}", event.sessionId());
            if (event.mentorEmail() != null) {
                emailService.sendTemplatedEmail(
                        event.mentorEmail(),
                        "Your session has been cancelled",
                        "session-cancellation",
                        Map.of("sessionTitle", event.sessionTitle(),
                               "cancellationReason", event.cancellationReason() != null ? event.cancellationReason() : "N/A")
                );
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing session.cancelled: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2), dltTopicSuffix = ".DLT")
    @KafkaListener(topics = KafkaTopics.SESSION_COMPLETED, groupId = "notification-session-completed")
    public void handleSessionCompleted(SessionCompletedEvent event, Acknowledgment ack) {
        try {
            log.info("Processing session completed for sessionId={}", event.sessionId());
            // In production, we'd fetch student emails and send rating-request emails
            log.info("Session completed: {} students attended, sending rating requests", event.attendedStudentIds().size());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing session.completed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2), dltTopicSuffix = ".DLT")
    @KafkaListener(topics = KafkaTopics.WAITLIST_PROMOTED, groupId = "notification-waitlist-promoted")
    public void handleWaitlistPromoted(WaitlistPromotedEvent event, Acknowledgment ack) {
        try {
            log.info("Waitlist promoted: sessionId={}, studentId={}", event.sessionId(), event.studentId());
            if (event.studentEmail() != null) {
                emailService.sendTemplatedEmail(
                        event.studentEmail(),
                        "Great news! A spot opened up in your waitlisted session",
                        "session-booking-confirmation",
                        Map.of("sessionTitle", event.sessionTitle() != null ? event.sessionTitle() : "Session",
                               "studentName", "Student",
                               "scheduledAt", "Check your booking")
                );
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing waitlist.promoted: {}", e.getMessage(), e);
            throw e;
        }
    }
}
