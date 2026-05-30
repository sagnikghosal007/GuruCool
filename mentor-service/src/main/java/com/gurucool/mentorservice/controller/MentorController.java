package com.gurucool.mentorservice.controller;

import com.gurucool.common.dto.ApiResponse;
import com.gurucool.common.dto.PagedResponse;
import com.gurucool.mentorservice.dto.*;
import com.gurucool.mentorservice.entity.MentorRating;
import com.gurucool.mentorservice.service.MentorProfileService;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Mentor Management", description = "Mentor profile, availability, ratings, and verification")
@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorProfileService mentorProfileService;

    @Operation(summary = "Create mentor profile", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> createProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-User-Name", required = false) String fullName,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @Valid @RequestBody CreateMentorProfileRequest request) {
        if (!"MENTOR".equals(role)) return ResponseEntity.status(403).body(ApiResponse.error("Only mentors can create mentor profiles"));
        MentorProfileResponse response = mentorProfileService.createProfile(UUID.fromString(userId), fullName, email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "Get own mentor profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> getOwnProfile(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.success(mentorProfileService.getProfileByUserId(UUID.fromString(userId))));
    }

    @Operation(summary = "Update mentor profile", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> updateOwnProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateMentorProfileRequest request) {
        MentorProfileResponse profile = mentorProfileService.getProfileByUserId(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(
                mentorProfileService.updateProfile(profile.id(), UUID.fromString(userId), request)));
    }

    @Operation(summary = "Search and list mentors")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MentorProfileResponse>>> searchMentors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "averageRating") String sort,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String verificationStatus) {
        MentorSearchRequest searchRequest = new MentorSearchRequest(null, company, null, null, verificationStatus, tag);
        PagedResponse<MentorProfileResponse> result = mentorProfileService.searchMentors(
                searchRequest, PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, sort)));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Get mentor by ID")
    @GetMapping("/{mentorId}")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> getMentor(@PathVariable UUID mentorId) {
        return ResponseEntity.ok(ApiResponse.success(mentorProfileService.getProfileById(mentorId)));
    }

    @Operation(summary = "Submit verification request", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{mentorId}/verify")
    public ResponseEntity<ApiResponse<String>> submitVerification(
            @PathVariable UUID mentorId,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String documentUrl) {
        UUID requestId = mentorProfileService.submitVerificationRequest(mentorId, UUID.fromString(userId), documentUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(requestId.toString(), "Verification request submitted"));
    }

    @Operation(summary = "Review verification request (College Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{mentorId}/verify/{requestId}")
    public ResponseEntity<ApiResponse<String>> reviewVerification(
            @PathVariable UUID mentorId,
            @PathVariable UUID requestId,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody VerificationReviewRequest request) {
        if (!"COLLEGE_ADMIN".equals(role) && !"PLATFORM_ADMIN".equals(role))
            return ResponseEntity.status(403).body(ApiResponse.error("Only admins can review verification requests"));
        mentorProfileService.reviewVerificationRequest(mentorId, requestId, UUID.fromString(adminId), request);
        return ResponseEntity.ok(ApiResponse.success("Verification request reviewed"));
    }

    @Operation(summary = "Set weekly availability", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{mentorId}/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> setAvailability(
            @PathVariable UUID mentorId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                mentorProfileService.setAvailability(mentorId, UUID.fromString(userId), request)));
    }

    @Operation(summary = "Get mentor availability")
    @GetMapping("/{mentorId}/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailability(@PathVariable UUID mentorId) {
        return ResponseEntity.ok(ApiResponse.success(mentorProfileService.getAvailability(mentorId)));
    }

    @Operation(summary = "Rate a mentor", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{mentorId}/rate")
    public ResponseEntity<ApiResponse<String>> rateMentor(
            @PathVariable UUID mentorId,
            @RequestHeader("X-User-Id") String studentId,
            @Valid @RequestBody RateMentorRequest request) {
        mentorProfileService.rateMentor(mentorId, UUID.fromString(studentId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Rating submitted successfully"));
    }

    @Operation(summary = "Get mentor ratings")
    @GetMapping("/{mentorId}/ratings")
    public ResponseEntity<ApiResponse<PagedResponse<MentorRating>>> getRatings(
            @PathVariable UUID mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                mentorProfileService.getRatings(mentorId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))));
    }

    @Operation(summary = "Get top 10 mentors")
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<MentorProfileResponse>>> getTopMentors() {
        return ResponseEntity.ok(ApiResponse.success(mentorProfileService.getTopMentors()));
    }
}
