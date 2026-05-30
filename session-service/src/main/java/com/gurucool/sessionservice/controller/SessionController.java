package com.gurucool.sessionservice.controller;

import com.gurucool.common.dto.PagedResponse;
import com.gurucool.sessionservice.dto.*;
import com.gurucool.sessionservice.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Tag(name = "Session Management", description = "APIs for creating and booking mentorship sessions")
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "Create a session", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Session created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<SessionResponse>> createSession(
            @RequestHeader("X-User-Id") String mentorId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateSessionRequest request) {
        if (!"MENTOR".equals(role)) {
            return ResponseEntity.status(403).body(com.gurucool.common.dto.ApiResponse.error("Only mentors can create sessions"));
        }
        SessionResponse response = sessionService.createSession(UUID.fromString(mentorId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(com.gurucool.common.dto.ApiResponse.success(response));
    }

    @Operation(summary = "List sessions with filters")
    @GetMapping
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<PagedResponse<SessionResponse>>> listSessions(
            @RequestParam(required = false) UUID mentorId,
            @RequestParam(defaultValue = "UPCOMING") String status,
            @RequestParam(required = false) Boolean isPaid,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Instant from = fromDate != null ? Instant.parse(fromDate) : null;
        Instant to = toDate != null ? Instant.parse(toDate) : null;
        return ResponseEntity.ok(com.gurucool.common.dto.ApiResponse.success(
                sessionService.listSessions(mentorId, status, isPaid, from, to,
                        PageRequest.of(page, Math.min(size, 100), Sort.by("scheduledAt")))));
    }

    @Operation(summary = "Get session by ID")
    @GetMapping("/{sessionId}")
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<SessionResponse>> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(com.gurucool.common.dto.ApiResponse.success(sessionService.getSession(sessionId)));
    }

    @Operation(summary = "Update a session", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{sessionId}")
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<SessionResponse>> updateSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") String mentorId,
            @Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.ok(com.gurucool.common.dto.ApiResponse.success(
                sessionService.updateSession(sessionId, UUID.fromString(mentorId), request)));
    }

    @Operation(summary = "Cancel a session", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<String>> cancelSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String reason) {
        sessionService.cancelSession(sessionId, UUID.fromString(userId), role, reason);
        return ResponseEntity.ok(com.gurucool.common.dto.ApiResponse.success("Session cancelled"));
    }

    @Operation(summary = "Book a session", description = "Idempotent — use X-Idempotency-Key header",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Session booked successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Session fully booked — added to waitlist")
    })
    @PostMapping("/{sessionId}/book")
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<BookingResponse>> bookSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") String studentId,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        BookingResponse response = sessionService.bookSession(sessionId, UUID.fromString(studentId), idempotencyKey);
        HttpStatus httpStatus = response.waitlistPosition() != null ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
        return ResponseEntity.status(httpStatus).body(com.gurucool.common.dto.ApiResponse.success(response));
    }

    @Operation(summary = "Cancel a booking", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{sessionId}/book/{bookingId}")
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<String>> cancelBooking(
            @PathVariable UUID sessionId,
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") String studentId) {
        sessionService.cancelBooking(sessionId, bookingId, UUID.fromString(studentId));
        return ResponseEntity.ok(com.gurucool.common.dto.ApiResponse.success("Booking cancelled"));
    }

    @Operation(summary = "Get student's booked sessions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my/booked")
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<PagedResponse<BookingResponse>>> getMyBookings(
            @RequestHeader("X-User-Id") String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(com.gurucool.common.dto.ApiResponse.success(
                sessionService.getStudentBookings(UUID.fromString(studentId),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookedAt")))));
    }

    @Operation(summary = "Get mentor's sessions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my/hosting")
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<PagedResponse<SessionResponse>>> getMySessions(
            @RequestHeader("X-User-Id") String mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(com.gurucool.common.dto.ApiResponse.success(
                sessionService.getMentorSessions(UUID.fromString(mentorId),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledAt")))));
    }

    @Operation(summary = "Update session status", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{sessionId}/status")
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<String>> updateStatus(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") String mentorId,
            @Valid @RequestBody UpdateSessionStatusRequest request) {
        sessionService.updateSessionStatus(sessionId, UUID.fromString(mentorId), request.status());
        return ResponseEntity.ok(com.gurucool.common.dto.ApiResponse.success("Session status updated"));
    }

    @Operation(summary = "Add session recording", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{sessionId}/recording")
    public ResponseEntity<com.gurucool.common.dto.ApiResponse<String>> addRecording(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") String mentorId,
            @RequestBody Map<String, Object> body) {
        String recordingUrl = (String) body.get("recordingUrl");
        Integer durationSeconds = (Integer) body.get("durationSeconds");
        sessionService.addRecording(sessionId, UUID.fromString(mentorId), recordingUrl, durationSeconds);
        return ResponseEntity.status(HttpStatus.CREATED).body(com.gurucool.common.dto.ApiResponse.success("Recording added"));
    }
}
