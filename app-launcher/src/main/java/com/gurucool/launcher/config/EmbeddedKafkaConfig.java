package com.gurucool.launcher.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
public class EmbeddedKafkaConfig {

    private static final String[] TOPICS = {
        "user.registered", "user.email.verification",
        "mentor.verification.requested", "mentor.verified",
        "session.booked", "session.cancelled", "session.completed",
        "payment.completed", "payment.failed", "payment.refunded",
        "payment.refund.requested", "waitlist.promoted", "notification.send",
        // DLT topics
        "user.registered.DLT", "user.email.verification.DLT",
        "session.booked.DLT", "session.cancelled.DLT",
        "payment.completed.DLT", "payment.refunded.DLT",
        "waitlist.promoted.DLT", "mentor.verified.DLT"
    };

    @Bean(destroyMethod = "destroy")
    @Primary
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        log.info("Starting Embedded Apache Kafka broker (ZooKeeper mode)...");
        EmbeddedKafkaZKBroker broker = new EmbeddedKafkaZKBroker(1, true, 3, TOPICS);
        broker.kafkaPorts(9092);
        broker.brokerListProperty("spring.kafka.bootstrap-servers");
        log.info("Embedded Kafka started on port 9092");
        return broker;
    }

    @Bean
    @Primary
    public ProducerFactory<String, Object> producerFactory(EmbeddedKafkaBroker broker) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        configs.put(ProducerConfig.RETRIES_CONFIG, 3);
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Primary
    public ConsumerFactory<String, Object> consumerFactory(EmbeddedKafkaBroker broker) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "com.gurucool.*");
        configs.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.Map");
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
