package com.gurucool.paymentservice.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * MockPaymentEngine — simulates a payment gateway without any external calls.
 *
 * In a production system, replace this with the actual payment provider SDK.
 * The interface contract remains identical, making the swap a 1-file change.
 */
@Slf4j
@Component
public class MockPaymentEngine {

    @Value("${payment.mock.secret:gurucool-mock-payment-secret-key}")
    private String mockSecret;

    @Value("${payment.mock.success-rate:95}")
    private int successRatePercent;

    /**
     * Creates a mock payment order. Always succeeds.
     */
    public MockOrderResult createOrder(UUID bookingId, BigDecimal amount, String currency) {
        String mockOrderId = "mock_order_" + UUID.randomUUID().toString().replace("-", "");
        String receipt = "booking-" + bookingId;
        log.info("[MOCK PAYMENT] Order created: orderId={}, amount={} {}", mockOrderId, amount, currency);
        return new MockOrderResult(mockOrderId, receipt, amount, currency);
    }

    /**
     * Simulates a payment capture. Fails based on configured success rate.
     */
    public MockCaptureResult capturePayment(String mockOrderId, boolean forceFailure) {
        String mockPaymentId = "mock_pay_" + UUID.randomUUID().toString().replace("-", "");
        boolean success = !forceFailure && (Math.random() * 100 < successRatePercent);

        if (success) {
            String signature = generateSignature(mockOrderId, mockPaymentId);
            log.info("[MOCK PAYMENT] Payment captured: orderId={}, paymentId={}", mockOrderId, mockPaymentId);
            return new MockCaptureResult(true, mockPaymentId, signature, null);
        } else {
            String reason = forceFailure ? "Forced failure for testing" : "Simulated payment decline";
            log.warn("[MOCK PAYMENT] Payment failed: orderId={}, reason={}", mockOrderId, reason);
            return new MockCaptureResult(false, null, null, reason);
        }
    }

    /**
     * Verifies the HMAC-SHA256 signature using the mock secret.
     */
    public boolean verifySignature(String mockOrderId, String mockPaymentId, String signature) {
        String expected = generateSignature(mockOrderId, mockPaymentId);
        boolean valid = expected.equals(signature);
        log.debug("[MOCK PAYMENT] Signature verification: orderId={}, valid={}", mockOrderId, valid);
        return valid;
    }

    /**
     * Simulates a refund. Always succeeds in mock mode.
     */
    public MockRefundResult refund(String mockPaymentId, BigDecimal amount, String reason) {
        String mockRefundId = "mock_refund_" + UUID.randomUUID().toString().replace("-", "");
        log.info("[MOCK PAYMENT] Refund processed: paymentId={}, refundId={}, amount={}", mockPaymentId, mockRefundId, amount);
        return new MockRefundResult(mockRefundId, amount, "processed");
    }

    private String generateSignature(String orderId, String paymentId) {
        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(mockSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    // Result types
    public record MockOrderResult(String mockOrderId, String receipt, BigDecimal amount, String currency) {}
    public record MockCaptureResult(boolean success, String mockPaymentId, String signature, String failureReason) {}
    public record MockRefundResult(String mockRefundId, BigDecimal amount, String status) {}
}
