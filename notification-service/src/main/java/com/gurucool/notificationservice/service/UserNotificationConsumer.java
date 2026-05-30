package com.gurucool.notificationservice.service;

import com.gurucool.common.constants.KafkaTopics;
import com.gurucool.common.event.UserEmailVerificationEvent;
import com.gurucool.common.event.UserRegisteredEvent;
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
public class UserNotificationConsumer {

    private final EmailService emailService;

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2),
                    dltTopicSuffix = ".DLT")
    @KafkaListener(topics = KafkaTopics.USER_EMAIL_VERIFICATION, groupId = "notification-user-verification")
    public void handleEmailVerification(UserEmailVerificationEvent event, Acknowledgment ack) {
        try {
            log.info("Processing email verification for userId={}", event.userId());
            emailService.sendTemplatedEmail(
                    event.email(),
                    "Verify your GuruCool email",
                    "email-verification",
                    Map.of(
                        "userName", event.fullName(),
                        "verificationToken", event.verificationToken(),
                        "verificationUrl", "http://localhost:8080/api/users/auth/verify-email?token=" + event.verificationToken()
                    )
            );
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing email verification event: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2), dltTopicSuffix = ".DLT")
    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "notification-user-registered")
    public void handleUserRegistered(UserRegisteredEvent event, Acknowledgment ack) {
        try {
            log.info("Processing welcome email for userId={}", event.userId());
            emailService.sendTemplatedEmail(
                    event.email(),
                    "Welcome to GuruCool! 🎓",
                    "welcome",
                    Map.of("userName", event.fullName(), "role", event.role())
            );
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing user.registered event: {}", e.getMessage(), e);
            throw e;
        }
    }
}
