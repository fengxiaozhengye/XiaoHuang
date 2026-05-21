package com.edusmart.entity;

import com.edusmart.enums.LearningStyle;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "student_profile")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private LearningStyle learningStyle;

    @Column(columnDefinition = "JSON")
    private String knowledgeLevel;

    @Column(columnDefinition = "JSON")
    private String weakPoints;

    @Column(columnDefinition = "JSON")
    private String strongPoints;

    @Column(columnDefinition = "JSON")
    private String preference;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
