package com.gurucool.mentorservice.repository;

import com.gurucool.mentorservice.entity.MentorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, UUID> {
    List<MentorAvailability> findByMentorIdAndIsActiveTrue(UUID mentorId);

    @Modifying
    @Query("DELETE FROM MentorAvailability a WHERE a.mentor.id = :mentorId")
    void deleteByMentorId(UUID mentorId);
}
