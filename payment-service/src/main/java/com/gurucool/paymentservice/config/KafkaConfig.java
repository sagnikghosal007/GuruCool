package com.gurucool.paymentservice.config;

import com.gurucool.common.constants.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean public NewTopic paymentCompletedTopic() { return TopicBuilder.name(KafkaTopics.PAYMENT_COMPLETED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic paymentFailedTopic() { return TopicBuilder.name(KafkaTopics.PAYMENT_FAILED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic paymentRefundedTopic() { return TopicBuilder.name(KafkaTopics.PAYMENT_REFUNDED).partitions(3).replicas(1).build(); }
}
