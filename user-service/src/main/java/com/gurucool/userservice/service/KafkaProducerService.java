package com.gurucool.userservice.service;

import com.gurucool.common.constants.KafkaTopics;
import com.gurucool.common.event.UserEmailVerificationEvent;
import com.gurucool.common.event.UserRegisteredEvent;
import com.gurucool.common.exception.KafkaPublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void publishUserRegistered(UserRegisteredEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.USER_REGISTERED, event.userId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish UserRegisteredEvent for userId={}: {}", event.userId(), ex.getMessage());
                        } else {
                            log.info("Published UserRegisteredEvent for userId={}", event.userId());
                        }
                    });
        } catch (Exception e) {
            throw new KafkaPublishException(KafkaTopics.USER_REGISTERED, e.getMessage());
        }
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void publishEmailVerification(UserEmailVerificationEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.USER_EMAIL_VERIFICATION, event.userId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish EmailVerificationEvent: {}", ex.getMessage());
                        } else {
                            log.info("Published EmailVerificationEvent for userId={}", event.userId());
                        }
                    });
        } catch (Exception e) {
            throw new KafkaPublishException(KafkaTopics.USER_EMAIL_VERIFICATION, e.getMessage());
        }
    }
}
