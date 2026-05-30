package com.gurucool.paymentservice.service;

import com.gurucool.common.dto.PagedResponse;
import com.gurucool.common.event.PaymentCompletedEvent;
import com.gurucool.common.event.PaymentFailedEvent;
import com.gurucool.common.event.PaymentRefundedEvent;
import com.gurucool.common.exception.PaymentException;
import com.gurucool.common.exception.ResourceNotFoundException;
import com.gurucool.paymentservice.dto.*;
import com.gurucool.paymentservice.entity.*;
import com.gurucool.paymentservice.mock.MockPaymentEngine;
import com.gurucool.paymentservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentLedgerRepository ledgerRepository;
    private final MentorPayoutRepository payoutRepository;
    private final MockPaymentEngine mockPaymentEngine;
    private final KafkaProducerService kafkaProducer;
    private final IdempotencyService idempotencyService;

    @Transactional
    public PaymentOrderResponse createOrder(String idempotencyKey, CreatePaymentOrderRequest req, UUID studentId) {
        Optional<PaymentOrderResponse> cached = idempotencyService.check(idempotencyKey, PaymentOrderResponse.class);
        if (cached.isPresent()) { log.info("Returning idempotent order for key={}", idempotencyKey); return cached.get(); }

        MockPaymentEngine.MockOrderResult mockOrder = mockPaymentEngine.createOrder(req.bookingId(), req.amount(), req.currency());

        PaymentOrder order = paymentOrderRepository.save(PaymentOrder.builder()
                .mockOrderId(mockOrder.mockOrderId())
                .bookingId(req.bookingId())
                .studentId(studentId)
                .mentorId(req.mentorId())
                .amount(req.amount())
                .currency(req.currency())
                .status(PaymentStatus.CREATED)
                .idempotencyKey(idempotencyKey)
                .build());

        ledgerRepository.save(PaymentLedger.builder()
                .paymentOrderId(order.getId())
                .eventType(LedgerEventType.DEBIT)
                .amount(req.amount())
                .description("Mock order created for booking: " + req.bookingId())
                .metadata("{\"mockOrderId\":\"" + mockOrder.mockOrderId() + "\"}")
                .build());

        PaymentOrderResponse response = new PaymentOrderResponse(
                order.getId(), mockOrder.mockOrderId(), req.bookingId(),
                req.amount(), req.currency(), PaymentStatus.CREATED.name(),
                null, "Use mockOrderId + POST /verify to complete payment in test mode");

        idempotencyService.store(idempotencyKey, response);
        log.info("Mock payment order created: id={}, mockOrderId={}", order.getId(), mockOrder.mockOrderId());
        return response;
    }

    @Transactional
    public Map<String, Object> verifyPayment(VerifyPaymentRequest req, UUID studentId) {
        if (!mockPaymentEngine.verifySignature(req.mockOrderId(), req.mockPaymentId(), req.mockSignature())) {
            throw new PaymentException("Mock payment signature verification failed", "MOCK_SIGNATURE_MISMATCH");
        }

        PaymentOrder order = paymentOrderRepository.findByMockOrderId(req.mockOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "mockOrderId", req.mockOrderId()));

        // Pessimistic lock for state transition
        PaymentOrder locked = paymentOrderRepository.findByIdForUpdate(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "id", order.getId()));

        if (locked.getStatus() == PaymentStatus.CAPTURED) {
            return Map.of("success", true, "paymentId", req.mockPaymentId(), "bookingId", req.bookingId(), "alreadyCaptured", true);
        }

        locked.setStatus(PaymentStatus.CAPTURED);
        locked.setMockPaymentId(req.mockPaymentId());
        locked.setMockSignature(req.mockSignature());
        paymentOrderRepository.save(locked);

        ledgerRepository.save(PaymentLedger.builder()
                .paymentOrderId(locked.getId())
                .eventType(LedgerEventType.CREDIT)
                .amount(locked.getAmount())
                .description("Mock payment captured: " + req.mockPaymentId())
                .metadata("{\"mockPaymentId\":\"" + req.mockPaymentId() + "\"}")
                .build());

        kafkaProducer.publishPaymentCompleted(new PaymentCompletedEvent(
                locked.getId(), locked.getBookingId(), locked.getStudentId(),
                null, locked.getMentorId(), locked.getAmount(), locked.getCurrency()));

        log.info("Mock payment captured: orderId={}, paymentId={}", req.mockOrderId(), req.mockPaymentId());
        return Map.of("success", true, "paymentId", req.mockPaymentId(), "bookingId", req.bookingId());
    }

    /**
     * One-shot test endpoint: creates order + simulates capture in a single call.
     * Ideal for integration testing and demos.
     */
    @Transactional
    public Map<String, Object> simulateFullPayment(SimulatePaymentRequest req, UUID studentId) {
        String idempotencyKey = "sim-" + UUID.randomUUID();

        // Create order
        MockPaymentEngine.MockOrderResult mockOrder = mockPaymentEngine.createOrder(
                req.bookingId(), req.amount(), req.currency() != null ? req.currency() : "INR");

        PaymentOrder order = paymentOrderRepository.save(PaymentOrder.builder()
                .mockOrderId(mockOrder.mockOrderId())
                .bookingId(req.bookingId())
                .studentId(studentId)
                .mentorId(req.mentorId())
                .amount(req.amount())
                .currency(req.currency() != null ? req.currency() : "INR")
                .status(PaymentStatus.CREATED)
                .idempotencyKey(idempotencyKey)
                .build());

        // Simulate capture
        MockPaymentEngine.MockCaptureResult capture = mockPaymentEngine.capturePayment(mockOrder.mockOrderId(), req.forceFailure());

        if (capture.success()) {
            order.setStatus(PaymentStatus.CAPTURED);
            order.setMockPaymentId(capture.mockPaymentId());
            order.setMockSignature(capture.signature());
            paymentOrderRepository.save(order);

            ledgerRepository.save(PaymentLedger.builder()
                    .paymentOrderId(order.getId())
                    .eventType(LedgerEventType.CREDIT)
                    .amount(req.amount())
                    .description("Simulated payment capture")
                    .build());

            kafkaProducer.publishPaymentCompleted(new PaymentCompletedEvent(
                    order.getId(), req.bookingId(), studentId, null, req.mentorId(), req.amount(), "INR"));

            log.info("Simulation SUCCESS: bookingId={}, amount={}", req.bookingId(), req.amount());
            return Map.of("success", true, "mockOrderId", mockOrder.mockOrderId(),
                    "mockPaymentId", capture.mockPaymentId(), "signature", capture.signature(),
                    "amount", req.amount(), "status", "CAPTURED");
        } else {
            order.setStatus(PaymentStatus.FAILED);
            order.setFailureReason(capture.failureReason());
            paymentOrderRepository.save(order);

            kafkaProducer.publishPaymentFailed(new PaymentFailedEvent(
                    order.getId(), req.bookingId(), studentId, null, capture.failureReason()));

            log.warn("Simulation FAILED: bookingId={}, reason={}", req.bookingId(), capture.failureReason());
            return Map.of("success", false, "mockOrderId", mockOrder.mockOrderId(),
                    "reason", capture.failureReason(), "status", "FAILED");
        }
    }

    @Transactional
    public Map<String, Object> initiateRefund(RefundRequest req) {
        PaymentOrder order = paymentOrderRepository.findById(req.paymentOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "id", req.paymentOrderId()));

        if (order.getStatus() != PaymentStatus.CAPTURED) {
            throw new PaymentException("Can only refund CAPTURED payments. Current status: " + order.getStatus(), "INVALID_STATE");
        }

        BigDecimal refundAmount = req.amount() != null ? req.amount() : order.getAmount();

        MockPaymentEngine.MockRefundResult refund = mockPaymentEngine.refund(
                order.getMockPaymentId(), refundAmount, req.reason());

        order.setStatus(PaymentStatus.REFUNDED);
        paymentOrderRepository.save(order);

        ledgerRepository.save(PaymentLedger.builder()
                .paymentOrderId(order.getId())
                .eventType(LedgerEventType.REFUND)
                .amount(refundAmount)
                .description("Refund: " + req.reason())
                .metadata("{\"mockRefundId\":\"" + refund.mockRefundId() + "\"}")
                .build());

        kafkaProducer.publishPaymentRefunded(new PaymentRefundedEvent(
                order.getId(), order.getBookingId(), order.getStudentId(), null, refundAmount));

        return Map.of("success", true, "mockRefundId", refund.mockRefundId(),
                "amount", refundAmount, "status", "REFUNDED");
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaymentOrder> getHistory(UUID studentId, Pageable pageable) {
        Page<PaymentOrder> page = paymentOrderRepository.findByStudentId(studentId, pageable);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PaymentOrder getById(UUID paymentId) {
        return paymentOrderRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
    }

    @Transactional(readOnly = true)
    public EarningsSummaryResponse getMentorEarnings(UUID mentorId) {
        BigDecimal total = paymentOrderRepository.sumCapturedAmountByMentorId(mentorId);
        long sessions = paymentOrderRepository.findByMentorId(mentorId,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
        return new EarningsSummaryResponse(total, BigDecimal.ZERO, sessions, "INR");
    }

    @Transactional(readOnly = true)
    public PaymentDashboardResponse getDashboard() {
        List<PaymentOrder> all = paymentOrderRepository.findAll();
        BigDecimal revenue = all.stream().filter(p -> p.getStatus() == PaymentStatus.CAPTURED)
                .map(PaymentOrder::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refunds = all.stream().filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .map(PaymentOrder::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long failed = all.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count();
        return new PaymentDashboardResponse(revenue, refunds, (long) all.size(), failed,
                revenue.subtract(refunds), "Mock payment engine — test data only");
    }

    @Transactional
    public void handleRefundRequest(UUID bookingId) {
        paymentOrderRepository.findAll().stream()
                .filter(p -> p.getBookingId().equals(bookingId) && p.getStatus() == PaymentStatus.CAPTURED)
                .findFirst()
                .ifPresent(order -> {
                    try {
                        initiateRefund(new RefundRequest(order.getId(), null, "Auto-refund: session cancelled"));
                    } catch (Exception e) {
                        log.error("Auto-refund failed for bookingId={}: {}", bookingId, e.getMessage(), e);
                    }
                });
    }
}
