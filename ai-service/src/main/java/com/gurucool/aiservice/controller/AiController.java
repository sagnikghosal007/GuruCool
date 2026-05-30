package com.gurucool.aiservice.controller;

import com.gurucool.aiservice.dto.*;
import com.gurucool.aiservice.service.AiMatchingService;
import com.gurucool.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Tag(name = "AI Services", description = "AI-powered mentor matching, career planning, and session insights")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiMatchingService aiMatchingService;

    @Operation(summary = "AI mentor matching", description = "Finds top mentor matches using LLM analysis",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/match")
    public CompletableFuture<ResponseEntity<ApiResponse<MentorMatchResult>>> matchMentors(
            @Valid @RequestBody MentorMatchRequest request) {
        log.info("Mentor matching request for studentId={}", request.studentId());
        return aiMatchingService.matchMentors(request)
                .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result, "AI matching completed")));
    }

    @Operation(summary = "Generate career roadmap",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/career-path")
    public CompletableFuture<ResponseEntity<ApiResponse<CareerPathResponse>>> generateCareerPath(
            @Valid @RequestBody CareerPathRequest request) {
        return aiMatchingService.generateCareerPath(request)
                .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result)));
    }

    @Operation(summary = "Analyze skill gaps",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/skill-gap")
    public CompletableFuture<ResponseEntity<ApiResponse<SkillGapResponse>>> analyzeSkillGap(
            @Valid @RequestBody SkillGapRequest request) {
        return aiMatchingService.analyzeSkillGap(request)
                .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result)));
    }

    @Operation(summary = "Generate session summary (Mentor only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/session-summary")
    public CompletableFuture<ResponseEntity<ApiResponse<SessionSummaryResponse>>> generateSummary(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody SessionSummaryRequest request) {
        if (!"MENTOR".equals(role)) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(403).body(ApiResponse.error("Only mentors can generate session summaries")));
        }
        return aiMatchingService.generateSessionSummary(request)
                .thenApply(result -> ResponseEntity.ok(ApiResponse.success(result)));
    }

    @Operation(summary = "Get cached mentor match result",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/match/cache/{studentId}")
    public ResponseEntity<ApiResponse<Object>> getCachedMatch(@PathVariable UUID studentId) {
        Object cached = aiMatchingService.getCachedMatch(studentId);
        if (cached == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No cached match found"));
        }
        return ResponseEntity.ok(ApiResponse.success(cached));
    }
}
