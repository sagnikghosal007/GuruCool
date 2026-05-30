package com.gurucool.paymentservice.service;

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

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        send(KafkaTopics.PAYMENT_COMPLETED, event.paymentOrderId().toString(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        send(KafkaTopics.PAYMENT_FAILED, event.paymentOrderId().toString(), event);
    }

    public void publishPaymentRefunded(PaymentRefundedEvent event) {
        send(KafkaTopics.PAYMENT_REFUNDED, event.paymentOrderId().toString(), event);
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload).whenComplete((r, ex) -> {
            if (ex != null) {
                log.error("Failed to publish to {}: {}", topic, ex.getMessage());
                throw new KafkaPublishException(topic, ex.getMessage());
            }
            log.info("Published event to topic={}", topic);
        });
    }
}
