package com.gurucool.userservice.service;

import com.gurucool.common.event.UserEmailVerificationEvent;
import com.gurucool.common.event.UserRegisteredEvent;
import com.gurucool.common.exception.DuplicateResourceException;
import com.gurucool.common.exception.ResourceNotFoundException;
import com.gurucool.common.exception.UnauthorizedException;
import com.gurucool.userservice.dto.*;
import com.gurucool.userservice.entity.RefreshToken;
import com.gurucool.userservice.entity.User;
import com.gurucool.userservice.entity.UserRole;
import com.gurucool.userservice.repository.RefreshTokenRepository;
import com.gurucool.userservice.repository.UserRepository;
import com.gurucool.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final String IDEMPOTENCY_PREFIX = "idempotency:refresh:";
    private static final String LOGIN_ATTEMPTS_PREFIX = "login:attempts:";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final KafkaProducerService kafkaProducerService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registering new user with email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User already exists with email: " + request.email());
        }

        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(UserRole.valueOf(request.role()))
                .collegeId(request.collegeId())
                .isEmailVerified(false)
                .isActive(true)
                .failedLoginAttempts(0)
                .build();

        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(Instant.now().plus(Duration.ofHours(24)));

        user = userRepository.save(user);
        log.info("User created with id={}", user.getId());

        // Publish events
        kafkaProducerService.publishUserRegistered(new UserRegisteredEvent(
                user.getId(), user.getEmail(), user.getRole().name(), user.getCollegeId(), user.getFullName()));

        kafkaProducerService.publishEmailVerification(new UserEmailVerificationEvent(
                user.getId(), user.getEmail(), verificationToken, user.getFullName()));

        return new RegisterResponse(user.getId(), user.getEmail(), user.getRole().name(),
                "Registration successful! Please verify your email.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.email());

        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Check if account is locked
        if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
            throw new UnauthorizedException("Account is locked. Please try again after 15 minutes.");
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated. Contact support.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Reset failed attempts on success
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenValue = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshTokenValue)
                .expiresAt(Instant.now().plus(Duration.ofDays(7)))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        log.info("User logged in successfully: userId={}", user.getId());
        MDC.put("userId", user.getId().toString());

        return new AuthResponse(accessToken, refreshTokenValue, "Bearer", 900, toProfileResponse(user));
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCKOUT_DURATION));
            log.warn("Account locked for email={} after {} failed attempts", user.getEmail(), attempts);
        }
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        // Idempotency check in Redis
        String idempotencyKey = IDEMPOTENCY_PREFIX + refreshTokenValue.hashCode();
        Object cachedResponse = redisTemplate.opsForValue().get(idempotencyKey);
        if (cachedResponse != null) {
            log.debug("Returning cached refresh token response");
            // In a real scenario, the cached AuthResponse would be deserialized. For simplicity, we proceed.
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getIsRevoked() || Instant.now().isAfter(refreshToken.getExpiresAt())) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", refreshToken.getUserId()));

        // Rotate refresh token
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken newToken = RefreshToken.builder()
                .userId(user.getId())
                .token(newRefreshToken)
                .expiresAt(Instant.now().plus(Duration.ofDays(7)))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(newToken);

        AuthResponse response = new AuthResponse(newAccessToken, newRefreshToken, "Bearer", 900, toProfileResponse(user));
        redisTemplate.opsForValue().set(idempotencyKey, "rotated", Duration.ofSeconds(5));

        return response;
    }

    @Transactional
    public void logout(String refreshTokenValue, String accessToken) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    refreshTokenRepository.save(token);
                });

        // Blacklist access token
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String token = accessToken.substring(7);
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "blacklisted", Duration.ofMinutes(15));
        }

        log.info("User logged out successfully");
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getEmailVerificationToken()))
                .filter(u -> Instant.now().isBefore(u.getEmailVerificationTokenExpiry()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired verification token"));

        user.setIsEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
        log.info("Email verified for userId={}", user.getId());
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getPhoneNumber(), user.getProfilePictureUrl(),
                user.getCollegeId(), user.getIsEmailVerified(), user.getIsActive(), user.getCreatedAt());
    }
}
