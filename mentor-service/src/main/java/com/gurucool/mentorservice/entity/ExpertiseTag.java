package com.gurucool.mentorservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expertise_tags", indexes = {
    @Index(name = "idx_expertise_mentor_id", columnList = "mentor_id"),
    @Index(name = "idx_expertise_tag", columnList = "tag")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExpertiseTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private MentorProfile mentor;

    @Column(nullable = false, length = 100)
    private String tag;

    @Column(length = 100)
    private String category;
}
