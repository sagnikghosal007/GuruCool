package com.gurucool.paymentservice.controller;

import com.gurucool.common.dto.ApiResponse;
import com.gurucool.common.dto.PagedResponse;
import com.gurucool.paymentservice.dto.*;
import com.gurucool.paymentservice.entity.PaymentOrder;
import com.gurucool.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Tag(name = "Payment Management", description = "Mock payment engine — test payment flows without real money")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
        summary = "Create mock payment order",
        description = "Creates a mock payment order. Returns a mockOrderId to use in /verify.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-User-Id") String studentId,
            @Valid @RequestBody CreatePaymentOrderRequest request) {
        PaymentOrderResponse response = paymentService.createOrder(idempotencyKey, request, UUID.fromString(studentId));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Mock order created"));
    }

    @Operation(
        summary = "Verify mock payment",
        description = "Verifies mock payment signature and marks order as CAPTURED. Use the signature from /orders response.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPayment(
            @RequestHeader("X-User-Id") String studentId,
            @Valid @RequestBody VerifyPaymentRequest request) {
        Map<String, Object> result = paymentService.verifyPayment(request, UUID.fromString(studentId));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(
        summary = "Simulate full payment (test only)",
        description = "One-shot endpoint: creates order + simulates capture. Set forceFailure=true to test failure path. Returns mock signature for manual verify testing.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/test/simulate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulatePayment(
            @RequestHeader("X-User-Id") String studentId,
            @Valid @RequestBody SimulatePaymentRequest request) {
        Map<String, Object> result = paymentService.simulateFullPayment(request, UUID.fromString(studentId));
        boolean success = (boolean) result.get("success");
        return ResponseEntity.status(success ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponse.success(result, success ? "Payment simulated successfully" : "Payment simulation failed"));
    }

    @Operation(
        summary = "Initiate refund",
        description = "Refunds a captured payment. Admin or auto-triggered.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiateRefund(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody RefundRequest request) {
        if (!"PLATFORM_ADMIN".equals(role))
            return ResponseEntity.status(403).body(ApiResponse.error("Admin access required"));
        return ResponseEntity.ok(ApiResponse.success(paymentService.initiateRefund(request)));
    }

    @Operation(summary = "Get payment history", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentOrder>>> getHistory(
            @RequestHeader("X-User-Id") String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getHistory(UUID.fromString(studentId),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))));
    }

    @Operation(summary = "Get payment by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentOrder>> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getById(paymentId)));
    }

    @Operation(summary = "Mentor earnings summary", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/mentor/earnings")
    public ResponseEntity<ApiResponse<EarningsSummaryResponse>> getEarnings(
            @RequestHeader("X-User-Id") String mentorId,
            @RequestHeader("X-User-Role") String role) {
        if (!"MENTOR".equals(role)) return ResponseEntity.status(403).body(ApiResponse.error("Mentor access required"));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getMentorEarnings(UUID.fromString(mentorId))));
    }

    @Operation(summary = "Admin payment dashboard", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/admin/dashboard")
    public ResponseEntity<ApiResponse<PaymentDashboardResponse>> getDashboard(
            @RequestHeader("X-User-Role") String role) {
        if (!"PLATFORM_ADMIN".equals(role)) return ResponseEntity.status(403).body(ApiResponse.error("Admin access required"));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getDashboard()));
    }
}
