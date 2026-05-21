package com.edusmart.repository;

import com.edusmart.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    Optional<LearningPath> findByUserIdAndCourseIdAndStatus(Long userId, Long courseId, String status);

    List<LearningPath> findByUserId(Long userId);

    List<LearningPath> findByUserIdAndStatus(Long userId, String status);
}
