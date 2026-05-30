package com.gurucool.userservice.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.profile-pictures}")
    private String profilePicturesBucket;

    public String uploadProfilePicture(UUID userId, MultipartFile file) {
        try {
            String objectName = "users/" + userId + "/" + UUID.randomUUID() + getExtension(file.getOriginalFilename());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(profilePicturesBucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(profilePicturesBucket)
                    .object(objectName)
                    .expiry(15, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("Failed to upload profile picture for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to upload profile picture", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}
