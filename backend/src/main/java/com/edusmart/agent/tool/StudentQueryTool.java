package com.edusmart.agent.tool;

import com.edusmart.entity.LearningRecord;
import com.edusmart.entity.StudentProfile;
import com.edusmart.repository.LearningRecordRepository;
import com.edusmart.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StudentQueryTool {

    private final StudentProfileRepository profileRepository;
    private final LearningRecordRepository recordRepository;

    /**
     * 查询学生画像信息
     */
    public Map<String, Object> getStudentProfile(Long userId) {
        StudentProfile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return Map.of("exists", false, "message", "该学生尚未完成初始测评");
        }
        return Map.of(
                "exists", true,
                "learningStyle", profile.getLearningStyle() != null ? profile.getLearningStyle().name() : "UNKNOWN",
                "knowledgeLevel", profile.getKnowledgeLevel() != null ? profile.getKnowledgeLevel() : "{}",
                "weakPoints", profile.getWeakPoints() != null ? profile.getWeakPoints() : "[]",
                "strongPoints", profile.getStrongPoints() != null ? profile.getStrongPoints() : "[]",
                "preference", profile.getPreference() != null ? profile.getPreference() : "{}"
        );
    }

    /**
     * 查询学生最近的学习记录
     */
    public List<Map<String, Object>> getRecentRecords(Long userId, int limit) {
        List<LearningRecord> records = recordRepository.findByUserId(userId);
        return records.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .map(r -> Map.<String, Object>of(
                        "knowledgePointId", r.getKnowledgePoint().getId(),
                        "knowledgePointName", r.getKnowledgePoint().getName(),
                        "action", r.getAction(),
                        "score", r.getScore() != null ? r.getScore() : "N/A",
                        "duration", r.getDurationSeconds() != null ? r.getDurationSeconds() : 0,
                        "time", r.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
    }
}
