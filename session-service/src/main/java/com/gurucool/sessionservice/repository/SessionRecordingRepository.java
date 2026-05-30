package com.gurucool.sessionservice.repository;

import com.gurucool.sessionservice.entity.SessionRecording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRecordingRepository extends JpaRepository<SessionRecording, UUID> {
    List<SessionRecording> findBySessionId(UUID sessionId);
}
