package com.gurucool.sessionservice.service;

import com.gurucool.common.constants.KafkaTopics;
import com.gurucool.common.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCompletedConsumer {

    private final SessionService sessionService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "session-service-payment-completed")
    public void handlePaymentCompleted(PaymentCompletedEvent event, Acknowledgment ack) {
        try {
            log.info("Received payment.completed for bookingId={}", event.bookingId());
            sessionService.confirmBookingOnPayment(event.bookingId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing payment.completed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
