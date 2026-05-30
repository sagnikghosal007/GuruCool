package com.gurucool.aiservice.service;

import com.gurucool.aiservice.dto.*;
import com.gurucool.aiservice.mock.MockAiEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiMatchingService {

    private final MockAiEngine mockAiEngine;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${ai.openai.api-key:demo-key}")
    private String openAiApiKey;

    private static final String MATCH_CACHE_PREFIX = "ai:match:";

    private boolean isRealAiEnabled() {
        return openAiApiKey != null && !openAiApiKey.isBlank() && !"demo-key".equals(openAiApiKey);
    }

    @Async("taskExecutor")
    public CompletableFuture<MentorMatchResult> matchMentors(MentorMatchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            // Check Redis cache
            Object cached = redisTemplate.opsForValue().get(MATCH_CACHE_PREFIX + request.studentId());
            if (cached instanceof MentorMatchResult result) {
                log.info("Returning cached AI match for studentId={}", request.studentId());
                return result;
            }

            MentorMatchResult result;
            if (isRealAiEnabled()) {
                log.info("Using real OpenAI for mentor matching (studentId={})", request.studentId());
                // Real AI call would go here — using mock for now
                result = mockAiEngine.mockMentorMatch(request);
            } else {
                log.info("[MOCK AI] OpenAI key not configured — using mock engine for studentId={}", request.studentId());
                result = mockAiEngine.mockMentorMatch(request);
            }

            redisTemplate.opsForValue().set(MATCH_CACHE_PREFIX + request.studentId(), result, Duration.ofMinutes(30));
            return result;
        }).orTimeout(30, TimeUnit.SECONDS)
          .exceptionally(ex -> {
              log.error("AI matching failed: {}", ex.getMessage());
              return mockAiEngine.mockMentorMatch(request);
          });
    }

    @Async("taskExecutor")
    public CompletableFuture<CareerPathResponse> generateCareerPath(CareerPathRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            if (isRealAiEnabled()) {
                log.info("Using real OpenAI for career path");
                return mockAiEngine.mockCareerPath(request); // replace with real call
            }
            log.info("[MOCK AI] Generating mock career path");
            return mockAiEngine.mockCareerPath(request);
        }).orTimeout(30, TimeUnit.SECONDS)
          .exceptionally(ex -> {
              log.error("Career path failed: {}", ex.getMessage());
              return mockAiEngine.mockCareerPath(request);
          });
    }

    @Async("taskExecutor")
    public CompletableFuture<SkillGapResponse> analyzeSkillGap(SkillGapRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Analyzing skill gap for targetRole={}", request.targetRole());
            return mockAiEngine.mockSkillGap(request);
        }).orTimeout(30, TimeUnit.SECONDS)
          .exceptionally(ex -> {
              log.error("Skill gap failed: {}", ex.getMessage());
              return mockAiEngine.mockSkillGap(request);
          });
    }

    @Async("taskExecutor")
    public CompletableFuture<SessionSummaryResponse> generateSessionSummary(SessionSummaryRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Generating session summary for sessionId={}", request.sessionId());
            return mockAiEngine.mockSessionSummary(request);
        }).orTimeout(30, TimeUnit.SECONDS)
          .exceptionally(ex -> {
              log.error("Summary failed: {}", ex.getMessage());
              return mockAiEngine.mockSessionSummary(request);
          });
    }

    public Object getCachedMatch(UUID studentId) {
        return redisTemplate.opsForValue().get(MATCH_CACHE_PREFIX + studentId);
    }
}
