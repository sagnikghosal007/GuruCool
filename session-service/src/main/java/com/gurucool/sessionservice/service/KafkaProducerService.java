package com.gurucool.sessionservice.service;

import com.gurucool.common.constants.KafkaTopics;
import com.gurucool.common.event.*;
import com.gurucool.common.exception.KafkaPublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSessionBooked(SessionBookedEvent event) {
        send(KafkaTopics.SESSION_BOOKED, event.sessionId().toString(), event);
    }

    public void publishSessionCancelled(SessionCancelledEvent event) {
        send(KafkaTopics.SESSION_CANCELLED, event.sessionId().toString(), event);
    }

    public void publishSessionCompleted(SessionCompletedEvent event) {
        send(KafkaTopics.SESSION_COMPLETED, event.sessionId().toString(), event);
    }

    public void publishRefundRequested(PaymentRefundRequestedEvent event) {
        send(KafkaTopics.PAYMENT_REFUND_REQUESTED, event.bookingId().toString(), event);
    }

    public void publishWaitlistPromoted(WaitlistPromotedEvent event) {
        send(KafkaTopics.WAITLIST_PROMOTED, event.sessionId().toString(), event);
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload).whenComplete((r, ex) -> {
            if (ex != null) {
                log.error("Failed to publish to topic={}: {}", topic, ex.getMessage());
                throw new KafkaPublishException(topic, ex.getMessage());
            }
            log.info("Published event to topic={}", topic);
        });
    }
}
