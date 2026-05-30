package com.gurucool.launcher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class LocalInfraConfig {

    /**
     * In-memory cache — replaces Redis for local dev.
     * All @Cacheable annotations work transparently.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        log.info("Local mode: Using ConcurrentMapCacheManager (in-memory) instead of Redis");
        return new ConcurrentMapCacheManager(
            "userProfiles", "mentorProfiles", "mentorAvailability",
            "topMentors", "aiMatches", "sessions"
        );
    }

    /**
     * Stub RedisTemplate — backed by no-op connection for code that
     * directly uses RedisTemplate (idempotency keys, rate limiting).
     * Operations that fail are caught gracefully in services.
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory,
                                                        ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * No-op mail sender — logs emails to console instead of sending.
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        log.info("Local mode: Using stub JavaMailSender — emails logged to console");
        return new JavaMailSenderImpl() {
            @Override
            public void send(org.springframework.mail.SimpleMailMessage simpleMessage) {
                log.info("[EMAIL STUB] To={}, Subject={}",
                        simpleMessage.getTo(), simpleMessage.getSubject());
            }
            @Override
            public void send(jakarta.mail.internet.MimeMessage mimeMessage) {
                log.info("[EMAIL STUB] MimeMessage sent (stub)");
            }
        };
    }

    /**
     * Stub MinIO client — logs file operations instead of uploading.
     */
    @Bean
    @Primary
    public MinioClient minioClient() {
        log.info("Local mode: Using stub MinioClient — file uploads logged to console");
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("stub", "stub123456")
                .build();
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
