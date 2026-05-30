package com.gurucool.userservice.repository;

import com.gurucool.userservice.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollegeRepository extends JpaRepository<College, UUID> {
    Optional<College> findByDomain(String domain);
    boolean existsByDomain(String domain);
}
