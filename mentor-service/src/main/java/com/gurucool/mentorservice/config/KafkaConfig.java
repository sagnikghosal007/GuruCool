package com.gurucool.mentorservice.config;

import com.gurucool.common.constants.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean public NewTopic mentorVerificationRequestedTopic() {
        return TopicBuilder.name(KafkaTopics.MENTOR_VERIFICATION_REQUESTED).partitions(3).replicas(1).build();
    }
    @Bean public NewTopic mentorVerifiedTopic() {
        return TopicBuilder.name(KafkaTopics.MENTOR_VERIFIED).partitions(3).replicas(1).build();
    }
}
