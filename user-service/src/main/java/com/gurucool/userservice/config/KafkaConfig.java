package com.gurucool.userservice.config;

import com.gurucool.common.constants.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name(KafkaTopics.USER_REGISTERED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic userEmailVerificationTopic() {
        return TopicBuilder.name(KafkaTopics.USER_EMAIL_VERIFICATION).partitions(3).replicas(1).build();
    }
}
