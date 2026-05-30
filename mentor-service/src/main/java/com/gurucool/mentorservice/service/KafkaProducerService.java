package com.gurucool.mentorservice.service;

import com.gurucool.common.constants.KafkaTopics;
import com.gurucool.common.event.MentorVerificationRequestedEvent;
import com.gurucool.common.event.MentorVerifiedEvent;
import com.gurucool.common.exception.KafkaPublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishVerificationRequested(MentorVerificationRequestedEvent event) {
        kafkaTemplate.send(KafkaTopics.MENTOR_VERIFICATION_REQUESTED, event.mentorId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) throw new KafkaPublishException(KafkaTopics.MENTOR_VERIFICATION_REQUESTED, ex.getMessage());
                    log.info("Published MentorVerificationRequestedEvent for mentorId={}", event.mentorId());
                });
    }

    public void publishMentorVerified(MentorVerifiedEvent event) {
        kafkaTemplate.send(KafkaTopics.MENTOR_VERIFIED, event.mentorId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) throw new KafkaPublishException(KafkaTopics.MENTOR_VERIFIED, ex.getMessage());
                    log.info("Published MentorVerifiedEvent for mentorId={}", event.mentorId());
                });
    }
}
