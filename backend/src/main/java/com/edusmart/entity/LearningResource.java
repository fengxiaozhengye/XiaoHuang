package com.edusmart.entity;

import com.edusmart.enums.ResourceType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "learning_resource")
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_point_id", nullable = false)
    private KnowledgePoint knowledgePoint;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String fileUrl;

    @Column(nullable = false)
    private Integer difficultyLevel = 1;

    private Boolean isAiGenerated = true;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
