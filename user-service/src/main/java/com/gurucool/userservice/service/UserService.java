package com.gurucool.userservice.service;

import com.gurucool.common.exception.ResourceNotFoundException;
import com.gurucool.userservice.dto.UpdateProfileRequest;
import com.gurucool.userservice.dto.UserProfileResponse;
import com.gurucool.userservice.entity.User;
import com.gurucool.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MinioService minioService;

    @Transactional(readOnly = true)
    @Cacheable(value = "userProfiles", key = "#userId")
    public UserProfileResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return toProfileResponse(user);
    }

    @Transactional
    @CacheEvict(value = "userProfiles", key = "#userId")
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.info("Updating profile for userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber());
        }

        user = userRepository.save(user);
        log.info("Profile updated for userId={}", userId);
        return toProfileResponse(user);
    }

    @Transactional
    @CacheEvict(value = "userProfiles", key = "#userId")
    public String uploadProfilePicture(UUID userId, MultipartFile file) {
        log.info("Uploading profile picture for userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String url = minioService.uploadProfilePicture(userId, file);
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        return url;
    }

    @Transactional
    @CacheEvict(value = "userProfiles", key = "#userId")
    public void setUserStatus(UUID userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(isActive);
        userRepository.save(user);
        log.info("User status updated: userId={}, isActive={}", userId, isActive);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getPhoneNumber(), user.getProfilePictureUrl(),
                user.getCollegeId(), user.getIsEmailVerified(), user.getIsActive(), user.getCreatedAt());
    }
}
