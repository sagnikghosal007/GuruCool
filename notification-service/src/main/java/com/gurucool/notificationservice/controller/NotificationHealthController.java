package com.gurucool.notificationservice.controller;

import com.gurucool.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Notification Service", description = "Health check for notification service")
@RestController
@RequestMapping("/api/notifications")
public class NotificationHealthController {

    @Operation(summary = "Notification service health check")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "service", "notification-service",
                "status", "UP",
                "consumers", "active"
        )));
    }
}
