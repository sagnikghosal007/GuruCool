package com.gurucool.userservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colleges", indexes = {
    @Index(name = "idx_colleges_domain", columnList = "domain")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class College extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, length = 100)
    private String domain;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String website;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;
}
