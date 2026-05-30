package com.gurucool.userservice.controller;

import com.gurucool.common.dto.ApiResponse;
import com.gurucool.userservice.dto.UpdateProfileRequest;
import com.gurucool.userservice.dto.UserProfileResponse;
import com.gurucool.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Tag(name = "User Management", description = "User profile operations")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get own profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getOwnProfile(
            @RequestHeader("X-User-Id") String userId) {
        UserProfileResponse profile = userService.getUserById(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @Operation(summary = "Update own profile", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse updated = userService.updateProfile(UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @Operation(summary = "Upload profile picture", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadProfilePicture(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file) {
        String url = userService.uploadProfilePicture(UUID.fromString(userId), file);
        return ResponseEntity.ok(ApiResponse.success(url, "Profile picture uploaded"));
    }

    @Operation(summary = "Get user by ID (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Role") String role) {
        if (!"PLATFORM_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    @Operation(summary = "Update user status (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<String>> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody Map<String, Boolean> body,
            @RequestHeader("X-User-Role") String role) {
        if (!"PLATFORM_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        boolean isActive = Boolean.TRUE.equals(body.get("isActive"));
        userService.setUserStatus(userId, isActive);
        return ResponseEntity.ok(ApiResponse.success("User status updated"));
    }
}
