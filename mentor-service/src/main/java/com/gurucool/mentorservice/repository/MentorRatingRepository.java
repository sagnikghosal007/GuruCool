package com.gurucool.mentorservice.repository;

import com.gurucool.mentorservice.entity.MentorRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MentorRatingRepository extends JpaRepository<MentorRating, UUID> {
    Page<MentorRating> findByMentorId(UUID mentorId, Pageable pageable);
    boolean existsBySessionIdAndStudentId(UUID sessionId, UUID studentId);
    Optional<MentorRating> findBySessionId(UUID sessionId);

    @Query("SELECT AVG(r.rating) FROM MentorRating r WHERE r.mentorId = :mentorId")
    Double calculateAverageRating(UUID mentorId);

    @Query("SELECT COUNT(r) FROM MentorRating r WHERE r.mentorId = :mentorId")
    long countByMentorId(UUID mentorId);
}
