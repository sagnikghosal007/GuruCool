package com.gurucool.mentorservice.service;

import com.gurucool.common.constants.KafkaTopics;
import com.gurucool.common.event.UserRegisteredEvent;
import com.gurucool.mentorservice.entity.MentorProfile;
import com.gurucool.mentorservice.entity.VerificationStatus;
import com.gurucool.mentorservice.repository.MentorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final MentorProfileRepository mentorProfileRepository;

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "mentor-service-user-registered",
                   containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event, Acknowledgment ack) {
        try {
            if (!"MENTOR".equals(event.role())) {
                ack.acknowledge();
                return;
            }

            if (mentorProfileRepository.existsByUserId(event.userId())) {
                log.debug("Mentor profile already exists for userId={}", event.userId());
                ack.acknowledge();
                return;
            }

            MentorProfile shell = MentorProfile.builder()
                    .userId(event.userId())
                    .fullName(event.fullName())
                    .email(event.email())
                    .collegeId(event.collegeId())
                    .verificationStatus(VerificationStatus.UNVERIFIED)
                    .bio("")
                    .build();

            mentorProfileRepository.save(shell);
            log.info("Auto-created mentor profile shell for userId={}", event.userId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing user.registered event for userId={}: {}", event.userId(), e.getMessage(), e);
            throw e;
        }
    }
}
