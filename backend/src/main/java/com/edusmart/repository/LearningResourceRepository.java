package com.edusmart.repository;

import com.edusmart.entity.LearningResource;
import com.edusmart.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {

    List<LearningResource> findByKnowledgePointId(Long knowledgePointId);

    List<LearningResource> findByKnowledgePointIdAndType(Long knowledgePointId, ResourceType type);

    List<LearningResource> findByKnowledgePointIdAndDifficultyLevel(Long knowledgePointId, Integer difficultyLevel);
}
