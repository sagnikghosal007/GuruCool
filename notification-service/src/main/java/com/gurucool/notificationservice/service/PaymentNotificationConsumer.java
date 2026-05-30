package com.gurucool.notificationservice.service;

import com.gurucool.common.constants.KafkaTopics;
import com.gurucool.common.event.PaymentCompletedEvent;
import com.gurucool.common.event.PaymentRefundedEvent;
import com.gurucool.common.event.MentorVerifiedEvent;
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
public class PaymentNotificationConsumer {

    private final EmailService emailService;

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2), dltTopicSuffix = ".DLT")
    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "notification-payment-completed")
    public void handlePaymentCompleted(PaymentCompletedEvent event, Acknowledgment ack) {
        try {
            log.info("Processing payment receipt for studentId={}", event.studentId());
            if (event.studentEmail() != null) {
                emailService.sendTemplatedEmail(
                        event.studentEmail(),
                        "Payment Received - GuruCool",
                        "payment-receipt",
                        Map.of("amount", event.amount(), "currency", event.currency(),
                               "bookingId", event.bookingId())
                );
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing payment.completed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2), dltTopicSuffix = ".DLT")
    @KafkaListener(topics = KafkaTopics.PAYMENT_REFUNDED, groupId = "notification-payment-refunded")
    public void handlePaymentRefunded(PaymentRefundedEvent event, Acknowledgment ack) {
        try {
            log.info("Processing refund notification for studentId={}", event.studentId());
            if (event.studentEmail() != null) {
                emailService.sendTemplatedEmail(
                        event.studentEmail(),
                        "Refund Processed - GuruCool",
                        "payment-receipt",
                        Map.of("amount", event.amount(), "type", "REFUND", "bookingId", event.bookingId())
                );
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing payment.refunded: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2), dltTopicSuffix = ".DLT")
    @KafkaListener(topics = KafkaTopics.MENTOR_VERIFIED, groupId = "notification-mentor-verified")
    public void handleMentorVerified(MentorVerifiedEvent event, Acknowledgment ack) {
        try {
            log.info("Processing mentor verified notification for mentorId={}", event.mentorId());
            if (event.mentorEmail() != null) {
                emailService.sendTemplatedEmail(
                        event.mentorEmail(),
                        "Congratulations! Your GuruCool profile is verified ✅",
                        "mentor-verified",
                        Map.of("mentorName", event.mentorName(), "status", event.status())
                );
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing mentor.verified: {}", e.getMessage(), e);
            throw e;
        }
    }
}
