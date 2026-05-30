package com.gurucool.mentorservice.repository;

import com.gurucool.mentorservice.entity.MentorProfile;
import com.gurucool.mentorservice.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID>, JpaSpecificationExecutor<MentorProfile> {
    Optional<MentorProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    @Query("SELECT m FROM MentorProfile m ORDER BY m.averageRating DESC, m.totalSessions DESC")
    List<MentorProfile> findTop10ByOrderByAverageRatingDescTotalSessionsDesc(Pageable pageable);

    Page<MentorProfile> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    @Query("SELECT m FROM MentorProfile m WHERE m.collegeId = :collegeId")
    Page<MentorProfile> findByCollegeId(UUID collegeId, Pageable pageable);
}
