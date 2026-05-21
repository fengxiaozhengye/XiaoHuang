package com.edusmart.agent.tool;

import com.edusmart.entity.KnowledgeDependency;
import com.edusmart.entity.KnowledgePoint;
import com.edusmart.repository.KnowledgeDependencyRepository;
import com.edusmart.repository.KnowledgePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KnowledgeGraphTool {

    private final KnowledgePointRepository knowledgePointRepository;
    private final KnowledgeDependencyRepository dependencyRepository;

    /**
     * 获取课程的完整知识图谱（供Agent分析）
     */
    public String getCourseKnowledgeGraphText(Long courseId) {
        List<KnowledgePoint> points = knowledgePointRepository.findByCourseId(courseId);
        List<KnowledgeDependency> dependencies = dependencyRepository.findByCourseId(courseId);

        StringBuilder sb = new StringBuilder();
        sb.append("【知识点列表】\n");
        for (KnowledgePoint kp : points) {
            sb.append(String.format("- ID:%d %s (难度:%d) %s\n",
                    kp.getId(), kp.getName(), kp.getDifficultyLevel(),
                    kp.getDescription() != null ? kp.getDescription() : ""));
        }

        sb.append("\n【依赖关系】\n");
        for (KnowledgeDependency dep : dependencies) {
            sb.append(String.format("- %s → %s (%s)\n",
                    dep.getSource().getName(), dep.getTarget().getName(), dep.getDependencyType()));
        }

        return sb.toString();
    }

    /**
     * 查询单个知识点的前置依赖
     */
    public List<Map<String, Object>> getPrerequisites(Long knowledgePointId) {
        List<KnowledgeDependency> deps = dependencyRepository.findByTargetId(knowledgePointId);
        return deps.stream()
                .map(d -> Map.<String, Object>of(
                        "id", d.getSource().getId(),
                        "name", d.getSource().getName(),
                        "type", d.getDependencyType()
                ))
                .collect(Collectors.toList());
    }
}
