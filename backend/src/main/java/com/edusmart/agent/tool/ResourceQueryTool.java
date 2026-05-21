package com.edusmart.agent.tool;

import com.edusmart.entity.LearningResource;
import com.edusmart.repository.LearningResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ResourceQueryTool {

    private final LearningResourceRepository resourceRepository;

    /**
     * 查询知识点下已有资源（避免重复生成）
     */
    public List<Map<String, Object>> getExistingResources(Long knowledgePointId) {
        List<LearningResource> resources = resourceRepository.findByKnowledgePointId(knowledgePointId);
        return resources.stream()
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "title", r.getTitle(),
                        "type", r.getType().name(),
                        "difficulty", r.getDifficultyLevel(),
                        "isAiGenerated", r.getIsAiGenerated()
                ))
                .collect(Collectors.toList());
    }
}
