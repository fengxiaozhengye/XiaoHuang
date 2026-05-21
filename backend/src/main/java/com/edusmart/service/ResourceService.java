package com.edusmart.service;

import com.edusmart.agent.GenerationAgent;
import com.edusmart.common.BusinessException;
import com.edusmart.entity.KnowledgePoint;
import com.edusmart.entity.LearningResource;
import com.edusmart.enums.ResourceType;
import com.edusmart.repository.KnowledgePointRepository;
import com.edusmart.repository.LearningResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final LearningResourceRepository resourceRepository;
    private final KnowledgePointRepository knowledgePointRepository;
    private final GenerationAgent generationAgent;

    public List<LearningResource> getResources(Long knowledgePointId, ResourceType type, Integer difficulty) {
        if (type != null) {
            return resourceRepository.findByKnowledgePointIdAndType(knowledgePointId, type);
        }
        if (difficulty != null) {
            return resourceRepository.findByKnowledgePointIdAndDifficultyLevel(knowledgePointId, difficulty);
        }
        return resourceRepository.findByKnowledgePointId(knowledgePointId);
    }

    public LearningResource getResource(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("资源不存在"));
    }

    /**
     * AI生成学习资源
     */
    public LearningResource generateResource(Long knowledgePointId, ResourceType type,
                                              Integer difficulty, String studentLevel) {
        KnowledgePoint kp = knowledgePointRepository.findById(knowledgePointId)
                .orElseThrow(() -> new BusinessException("知识点不存在"));

        String prompt = String.format(
                "请为知识点「%s」生成一份%s类型的资源。\n知识点描述：%s\n目标难度：%d/5\n学生当前水平：%s",
                kp.getName(), type.name(),
                kp.getDescription() != null ? kp.getDescription() : "无",
                difficulty != null ? difficulty : 3,
                studentLevel != null ? studentLevel : "中级"
        );

        String content = generationAgent.chat(prompt, null);

        LearningResource resource = new LearningResource();
        resource.setKnowledgePoint(kp);
        resource.setTitle(kp.getName() + " - " + getTypeName(type));
        resource.setType(type);
        resource.setContent(content);
        resource.setDifficultyLevel(difficulty != null ? difficulty : 3);
        resource.setIsAiGenerated(true);

        return resourceRepository.save(resource);
    }

    private String getTypeName(ResourceType type) {
        return switch (type) {
            case TEXT -> "讲解";
            case CODE_EXAMPLE -> "代码示例";
            case EXERCISE -> "练习题";
            case MIND_MAP -> "思维导图";
            case VIDEO_SCRIPT -> "视频脚本";
        };
    }
}
