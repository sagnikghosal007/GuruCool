package com.gurucool.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurucool.paymentservice.entity.IdempotencyRecord;
import com.gurucool.paymentservice.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String REDIS_PREFIX = "idempotency:payment:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;
    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    public <T> Optional<T> check(String key, Class<T> type) {
        // Layer 1: Redis
        Object cached = redisTemplate.opsForValue().get(REDIS_PREFIX + key);
        if (cached != null) {
            log.debug("Idempotency HIT (Redis) key={}", key);
            try { return Optional.of(objectMapper.convertValue(cached, type)); }
            catch (Exception e) { log.warn("Redis deserialize failed: {}", e.getMessage()); }
        }
        // Layer 2: PostgreSQL
        return repository.findByIdempotencyKey(key).map(rec -> {
            log.debug("Idempotency HIT (DB) key={}", key);
            try {
                T r = objectMapper.readValue(rec.getResponsePayload(), type);
                redisTemplate.opsForValue().set(REDIS_PREFIX + key, r, TTL);
                return r;
            } catch (JsonProcessingException e) { return null; }
        });
    }

    @Transactional
    public <T> void store(String key, T response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(REDIS_PREFIX + key, response, TTL);
            repository.save(IdempotencyRecord.builder()
                    .idempotencyKey(key)
                    .responsePayload(json)
                    .serviceName("payment-service")
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plus(TTL))
                    .build());
        } catch (JsonProcessingException e) {
            log.error("Failed to store idempotency record: {}", e.getMessage());
        }
    }
}
