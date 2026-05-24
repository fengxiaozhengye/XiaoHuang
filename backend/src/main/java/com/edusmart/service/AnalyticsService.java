package com.edusmart.service;

import com.edusmart.entity.LearningPath;
import com.edusmart.entity.LearningRecord;
import com.edusmart.repository.LearningPathRepository;
import com.edusmart.repository.LearningRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LearningRecordRepository recordRepository;
    private final LearningPathRepository pathRepository;

    public Map<String, Object> getOverview(Long userId) {
        List<LearningRecord> records = recordRepository.findByUserId(userId);
        List<LearningPath> paths = pathRepository.findByUserId(userId);

        long totalDuration = records.stream()
                .filter(r -> r.getDurationSeconds() != null)
                .mapToLong(LearningRecord::getDurationSeconds)
                .sum();

        long completedKPs = records.stream()
                .filter(r -> "COMPLETE".equals(r.getAction()))
                .count();

        long activePaths = paths.stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("totalDurationMinutes", totalDuration / 60);
        result.put("completedKnowledgePoints", completedKPs);
        result.put("activePaths", activePaths);
        result.put("totalRecords", records.size());
        return result;
    }

    public Map<String, Object> getProgress(Long userId, Long courseId) {
        LearningPath path = pathRepository.findByUserIdAndCourseIdAndStatus(userId, courseId, "ACTIVE")
                .orElse(null);
        if (path == null) {
            return Map.of("exists", false);
        }
        return Map.of(
                "exists", true,
                "currentStep", path.getCurrentStep(),
                "totalSteps", path.getTotalSteps(),
                "progress", path.getTotalSteps() > 0
                        ? (double) path.getCurrentStep() / path.getTotalSteps() * 100 : 0,
                "status", path.getStatus(),
                "pathData", path.getPathData() != null ? path.getPathData() : "{}"
        );
    }
}
