package com.gurucool.mentorservice.service;

import com.gurucool.common.dto.PagedResponse;
import com.gurucool.common.event.MentorVerificationRequestedEvent;
import com.gurucool.common.event.MentorVerifiedEvent;
import com.gurucool.common.exception.DuplicateResourceException;
import com.gurucool.common.exception.ResourceNotFoundException;
import com.gurucool.common.exception.UnauthorizedException;
import com.gurucool.mentorservice.dto.*;
import com.gurucool.mentorservice.entity.*;
import com.gurucool.mentorservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorRatingRepository mentorRatingRepository;
    private final MentorVerificationRequestRepository verificationRequestRepository;
    private final MentorAvailabilityRepository availabilityRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public MentorProfileResponse createProfile(UUID userId, String fullName, String email,
                                                CreateMentorProfileRequest request) {
        if (mentorProfileRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("Mentor profile already exists for userId: " + userId);
        }

        MentorProfile profile = MentorProfile.builder()
                .userId(userId)
                .fullName(fullName)
                .email(email)
                .bio(request.bio())
                .currentCompany(request.currentCompany())
                .currentRole(request.currentRole())
                .experienceYears(request.experienceYears())
                .linkedinUrl(request.linkedinUrl())
                .collegeId(request.collegeId())
                .verificationStatus(VerificationStatus.UNVERIFIED)
                .build();

        if (request.expertiseTags() != null) {
            List<ExpertiseTag> tags = IntStream.range(0, request.expertiseTags().size())
                    .mapToObj(i -> ExpertiseTag.builder()
                            .mentor(profile)
                            .tag(request.expertiseTags().get(i))
                            .category(request.expertiseCategories() != null && i < request.expertiseCategories().size()
                                    ? request.expertiseCategories().get(i) : null)
                            .build())
                    .collect(Collectors.toList());
            profile.getExpertiseTags().addAll(tags);
        }

        MentorProfile saved = mentorProfileRepository.save(profile);
        log.info("Mentor profile created for userId={}, profileId={}", userId, saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mentorProfiles", key = "#mentorId")
    public MentorProfileResponse getProfileById(UUID mentorId) {
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("MentorProfile", "id", mentorId));
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public MentorProfileResponse getProfileByUserId(UUID userId) {
        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("MentorProfile", "userId", userId));
        return toResponse(profile);
    }

    @Transactional
    @CacheEvict(value = "mentorProfiles", key = "#mentorId")
    public MentorProfileResponse updateProfile(UUID mentorId, UUID userId, UpdateMentorProfileRequest request) {
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("MentorProfile", "id", mentorId));

        if (!profile.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own mentor profile");
        }

        if (request.bio() != null) profile.setBio(request.bio());
        if (request.currentCompany() != null) profile.setCurrentCompany(request.currentCompany());
        if (request.currentRole() != null) profile.setCurrentRole(request.currentRole());
        if (request.experienceYears() != null) profile.setExperienceYears(request.experienceYears());
        if (request.linkedinUrl() != null) profile.setLinkedinUrl(request.linkedinUrl());
        if (request.collegeId() != null) profile.setCollegeId(request.collegeId());

        if (request.expertiseTags() != null) {
            profile.getExpertiseTags().clear();
            List<ExpertiseTag> newTags = IntStream.range(0, request.expertiseTags().size())
                    .mapToObj(i -> ExpertiseTag.builder()
                            .mentor(profile)
                            .tag(request.expertiseTags().get(i))
                            .category(request.expertiseCategories() != null && i < request.expertiseCategories().size()
                                    ? request.expertiseCategories().get(i) : null)
                            .build())
                    .collect(Collectors.toList());
            profile.getExpertiseTags().addAll(newTags);
        }

        return toResponse(mentorProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MentorProfileResponse> searchMentors(MentorSearchRequest searchRequest, Pageable pageable) {
        Page<MentorProfile> page = mentorProfileRepository.findAll(pageable);
        return PagedResponse.from(page, this::toResponse);
    }

    @Transactional
    public UUID submitVerificationRequest(UUID mentorId, UUID userId, String documentUrl) {
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("MentorProfile", "id", mentorId));

        if (!profile.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only submit verification for your own profile");
        }

        profile.setVerificationStatus(VerificationStatus.PENDING);
        mentorProfileRepository.save(profile);

        MentorVerificationRequest request = MentorVerificationRequest.builder()
                .mentorId(mentorId)
                .collegeId(profile.getCollegeId())
                .status(VerificationStatus.PENDING)
                .documentUrl(documentUrl)
                .build();
        request = verificationRequestRepository.save(request);

        kafkaProducerService.publishVerificationRequested(new MentorVerificationRequestedEvent(
                mentorId, profile.getCollegeId(), documentUrl, profile.getFullName()));

        return request.getId();
    }

    @Transactional
    @CacheEvict(value = "mentorProfiles", key = "#mentorId")
    public void reviewVerificationRequest(UUID mentorId, UUID requestId, UUID adminId,
                                           VerificationReviewRequest reviewRequest) {
        MentorVerificationRequest verReq = verificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("VerificationRequest", "id", requestId));

        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("MentorProfile", "id", mentorId));

        VerificationStatus newStatus = reviewRequest.approved() ? VerificationStatus.INNER_CIRCLE : VerificationStatus.OUTER_CIRCLE;

        verReq.setStatus(newStatus);
        verReq.setReviewNote(reviewRequest.reviewNote());
        verReq.setReviewedAt(Instant.now());
        verReq.setReviewedBy(adminId);
        verificationRequestRepository.save(verReq);

        profile.setVerificationStatus(newStatus);
        mentorProfileRepository.save(profile);

        kafkaProducerService.publishMentorVerified(new MentorVerifiedEvent(
                mentorId, profile.getUserId(), profile.getCollegeId(), newStatus.name(),
                profile.getFullName(), profile.getEmail()));
    }

    @Transactional
    @CacheEvict(value = "mentorAvailability", key = "#mentorId")
    public AvailabilityResponse setAvailability(UUID mentorId, UUID userId, AvailabilityRequest request) {
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("MentorProfile", "id", mentorId));

        if (!profile.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own availability");
        }

        availabilityRepository.deleteByMentorId(mentorId);

        List<MentorAvailability> slots = request.slots().stream()
                .map(slot -> MentorAvailability.builder()
                        .mentor(profile)
                        .dayOfWeek(slot.dayOfWeek())
                        .startTime(slot.startTime())
                        .endTime(slot.endTime())
                        .isActive(true)
                        .build())
                .collect(Collectors.toList());

        List<MentorAvailability> saved = availabilityRepository.saveAll(slots);

        return new AvailabilityResponse(mentorId, saved.stream()
                .map(s -> new AvailabilityResponse.SlotResponse(s.getId(), s.getDayOfWeek(), s.getStartTime(), s.getEndTime(), s.getIsActive()))
                .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mentorAvailability", key = "#mentorId")
    public AvailabilityResponse getAvailability(UUID mentorId) {
        List<MentorAvailability> slots = availabilityRepository.findByMentorIdAndIsActiveTrue(mentorId);
        return new AvailabilityResponse(mentorId, slots.stream()
                .map(s -> new AvailabilityResponse.SlotResponse(s.getId(), s.getDayOfWeek(), s.getStartTime(), s.getEndTime(), s.getIsActive()))
                .collect(Collectors.toList()));
    }

    @Transactional
    public void rateMentor(UUID mentorId, UUID studentId, RateMentorRequest request) {
        if (mentorRatingRepository.existsBySessionIdAndStudentId(request.sessionId(), studentId)) {
            throw new DuplicateResourceException("You have already rated this mentor for this session");
        }

        MentorRating rating = MentorRating.builder()
                .mentorId(mentorId)
                .studentId(studentId)
                .sessionId(request.sessionId())
                .rating(request.rating())
                .feedback(request.feedback())
                .isAnonymous(request.isAnonymous())
                .build();

        mentorRatingRepository.save(rating);
        recalculateAverageRating(mentorId);
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> recalculateAverageRating(UUID mentorId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Double avg = mentorRatingRepository.calculateAverageRating(mentorId);
                long count = mentorRatingRepository.countByMentorId(mentorId);

                mentorProfileRepository.findById(mentorId).ifPresent(profile -> {
                    profile.setAverageRating(avg != null
                            ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);
                    profile.setTotalRatings((int) count);
                    mentorProfileRepository.save(profile);
                    log.info("Recalculated average rating for mentorId={}: {}", mentorId, avg);
                });
            } catch (Exception e) {
                log.error("Failed to recalculate rating for mentorId={}: {}", mentorId, e.getMessage(), e);
            }
        });
    }

    @Transactional(readOnly = true)
    public PagedResponse<MentorRating> getRatings(UUID mentorId, Pageable pageable) {
        Page<MentorRating> page = mentorRatingRepository.findByMentorId(mentorId, pageable);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "topMentors")
    public List<MentorProfileResponse> getTopMentors() {
        return mentorProfileRepository
                .findTop10ByOrderByAverageRatingDescTotalSessionsDesc(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "averageRating")))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private MentorProfileResponse toResponse(MentorProfile p) {
        List<String> tags = p.getExpertiseTags() != null
                ? p.getExpertiseTags().stream().map(ExpertiseTag::getTag).collect(Collectors.toList())
                : List.of();
        return new MentorProfileResponse(p.getId(), p.getUserId(), p.getFullName(), p.getEmail(),
                p.getBio(), p.getCurrentCompany(), p.getCurrentRole(), p.getExperienceYears(),
                p.getLinkedinUrl(), p.getVerificationStatus().name(), p.getCollegeId(),
                p.getAverageRating(), p.getTotalSessions(), p.getTotalRatings(),
                p.getProfilePictureUrl(), tags, p.getCreatedAt());
    }
}
