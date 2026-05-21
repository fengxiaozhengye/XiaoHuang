package com.edusmart.repository;

import com.edusmart.entity.LearningRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {

    List<LearningRecord> findByUserId(Long userId);

    List<LearningRecord> findByUserIdAndKnowledgePointId(Long userId, Long knowledgePointId);
}
