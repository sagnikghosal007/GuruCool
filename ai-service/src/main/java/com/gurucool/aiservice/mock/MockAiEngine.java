package com.gurucool.aiservice.mock;

import com.gurucool.aiservice.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MockAiEngine — returns realistic stub responses when OpenAI key is not configured.
 * Swap for real ChatClient calls once OPENAI_API_KEY is set.
 */
@Slf4j
@Component
public class MockAiEngine {

    public MentorMatchResult mockMentorMatch(MentorMatchRequest request) {
        log.info("[MOCK AI] Generating mentor matches for studentId={}", request.studentId());
        return new MentorMatchResult(List.of(
            new MentorMatchResult.MentorMatch("mentor-001", 94, "Strong " + request.preferredDomain() + " background aligns perfectly with your career goal."),
            new MentorMatchResult.MentorMatch("mentor-002", 87, "8 years experience in " + request.preferredDomain() + " with a 4.8 rating."),
            new MentorMatchResult.MentorMatch("mentor-003", 81, "Industry leader whose expertise matches your learning style.")
        ));
    }

    public CareerPathResponse mockCareerPath(CareerPathRequest request) {
        log.info("[MOCK AI] Generating career path for targetRole={}", request.targetRole());
        return new CareerPathResponse(List.of(
            new CareerPathResponse.Phase(1, "0-3 months",
                List.of("Core fundamentals", "Version control", "Problem solving"),
                List.of("CS50 on edX", "Clean Code by Robert Martin"),
                "Build Solid Foundation"),
            new CareerPathResponse.Phase(2, "3-6 months",
                List.of("System design basics", "Data structures", "APIs"),
                List.of("Designing Data-Intensive Applications", "LeetCode Top 100"),
                "Master Core Technical Skills"),
            new CareerPathResponse.Phase(3, "6-12 months",
                List.of("Production systems", "Team collaboration", "Leadership"),
                List.of("Staff Engineer mentorship", "Open source contribution"),
                "Land " + request.targetRole() + " Role")
        ));
    }

    public SkillGapResponse mockSkillGap(SkillGapRequest request) {
        log.info("[MOCK AI] Analyzing skill gap for targetRole={}", request.targetRole());
        List<String> current = request.currentSkills();
        List<String> missing = List.of("System Design", "Distributed Systems", "Cloud Architecture")
                .stream().filter(s -> !current.contains(s)).toList();
        return new SkillGapResponse(
            missing,
            missing.stream().limit(3).toList(),
            missing.size() * 8
        );
    }

    public SessionSummaryResponse mockSessionSummary(SessionSummaryRequest request) {
        log.info("[MOCK AI] Generating session summary for sessionId={}", request.sessionId());
        return new SessionSummaryResponse(
            "The mentorship session covered key technical concepts and career strategies relevant to the student's goals.",
            List.of(
                "Understood the importance of building in public and networking",
                "Identified top 3 skills to focus on in the next 3 months",
                "Learned about the mentor's career trajectory and lessons learned"
            ),
            List.of(
                "Complete the recommended course within 2 weeks",
                "Build a small project using the discussed technology stack",
                "Connect with 3 professionals in the target industry on LinkedIn",
                "Schedule a follow-up session in 4 weeks to review progress"
            )
        );
    }
}
