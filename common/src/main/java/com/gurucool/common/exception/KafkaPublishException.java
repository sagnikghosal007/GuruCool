package com.gurucool.common.exception;

public class KafkaPublishException extends RuntimeException {
    private final String topic;

    public KafkaPublishException(String topic, String message) {
        super(String.format("Failed to publish event to Kafka topic '%s': %s", topic, message));
        this.topic = topic;
    }

    public String getTopic() { return topic; }
}
