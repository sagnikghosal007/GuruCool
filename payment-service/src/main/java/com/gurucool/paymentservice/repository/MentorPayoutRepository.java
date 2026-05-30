package com.gurucool.paymentservice.repository;

import com.gurucool.paymentservice.entity.MentorPayout;
import com.gurucool.paymentservice.entity.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MentorPayoutRepository extends JpaRepository<MentorPayout, UUID> {
    Page<MentorPayout> findByMentorId(UUID mentorId, Pageable pageable);
    Page<MentorPayout> findByStatus(PayoutStatus status, Pageable pageable);
}
