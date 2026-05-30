package com.gurucool.mentorservice.repository;

import com.gurucool.mentorservice.entity.MentorVerificationRequest;
import com.gurucool.mentorservice.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MentorVerificationRequestRepository extends JpaRepository<MentorVerificationRequest, UUID> {
    Page<MentorVerificationRequest> findByMentorId(UUID mentorId, Pageable pageable);
    Optional<MentorVerificationRequest> findByMentorIdAndStatus(UUID mentorId, VerificationStatus status);
}
