package com.gurucool.sessionservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_recordings", indexes = {
    @Index(name = "idx_recording_session_id", columnList = "session_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionRecording extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "recording_url", nullable = false, length = 500)
    private String recordingUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;
}
