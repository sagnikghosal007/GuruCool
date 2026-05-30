package com.gurucool.paymentservice.service;

import com.gurucool.common.constants.KafkaTopics;
import com.gurucool.common.event.PaymentRefundRequestedEvent;
import com.gurucool.common.event.SessionCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCancelledConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = KafkaTopics.SESSION_CANCELLED, groupId = "payment-service-session-cancelled")
    public void handleSessionCancelled(SessionCancelledEvent event, Acknowledgment ack) {
        try {
            log.info("Session cancelled: sessionId={}, students={}", event.sessionId(), event.studentIds().size());
            // Auto-refund handled by PAYMENT_REFUND_REQUESTED events per booking
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing session.cancelled: {}", e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_REFUND_REQUESTED, groupId = "payment-service-refund-requested")
    public void handleRefundRequested(PaymentRefundRequestedEvent event, Acknowledgment ack) {
        try {
            log.info("Refund requested for bookingId={}", event.bookingId());
            paymentService.handleRefundRequest(event.bookingId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing payment.refund.requested: {}", e.getMessage(), e);
            throw e;
        }
    }
}
