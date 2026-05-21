package com.edusmart.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "knowledge_dependency",
       uniqueConstraints = @UniqueConstraint(columnNames = {"source_id", "target_id"}))
public class KnowledgeDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private KnowledgePoint source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private KnowledgePoint target;

    @Column(nullable = false, length = 20)
    private String dependencyType = "REQUIRED";
}
