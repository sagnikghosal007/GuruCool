package com.gurucool.sessionservice.config;

import com.gurucool.common.constants.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean public NewTopic sessionBookedTopic() { return TopicBuilder.name(KafkaTopics.SESSION_BOOKED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic sessionCancelledTopic() { return TopicBuilder.name(KafkaTopics.SESSION_CANCELLED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic sessionCompletedTopic() { return TopicBuilder.name(KafkaTopics.SESSION_COMPLETED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic paymentRefundRequestedTopic() { return TopicBuilder.name(KafkaTopics.PAYMENT_REFUND_REQUESTED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic waitlistPromotedTopic() { return TopicBuilder.name(KafkaTopics.WAITLIST_PROMOTED).partitions(3).replicas(1).build(); }
}
