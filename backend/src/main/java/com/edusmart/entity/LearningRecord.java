package com.edusmart.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "learning_record")
public class LearningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_point_id", nullable = false)
    private KnowledgePoint knowledgePoint;

    private Long resourceId;

    @Column(nullable = false, length = 20)
    private String action;

    private BigDecimal score;

    private Integer durationSeconds;

    @Column(columnDefinition = "JSON")
    private String detail;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
