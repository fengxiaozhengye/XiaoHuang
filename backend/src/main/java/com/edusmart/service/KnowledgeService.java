package com.edusmart.service;

import com.edusmart.common.BusinessException;
import com.edusmart.entity.Course;
import com.edusmart.entity.KnowledgeDependency;
import com.edusmart.entity.KnowledgePoint;
import com.edusmart.repository.CourseRepository;
import com.edusmart.repository.KnowledgeDependencyRepository;
import com.edusmart.repository.KnowledgePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgePointRepository knowledgePointRepository;
    private final KnowledgeDependencyRepository dependencyRepository;
    private final CourseRepository courseRepository;

    public List<KnowledgePoint> getKnowledgePointsByCourse(Long courseId) {
        return knowledgePointRepository.findByCourseId(courseId);
    }

    public KnowledgePoint getKnowledgePoint(Long id) {
        return knowledgePointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识点不存在"));
    }

    public List<KnowledgeDependency> getDependenciesByCourse(Long courseId) {
        return dependencyRepository.findByCourseId(courseId);
    }

    @Transactional
    public Map<String, Object> importKnowledge(Long courseId, List<Map<String, Object>> points,
                                                List<Map<String, Object>> dependencies) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("课程不存在"));

        // 导入知识点
        Map<Long, KnowledgePoint> idMap = new HashMap<>();
        int pointCount = 0;
        for (Map<String, Object> pointData : points) {
            KnowledgePoint kp = new KnowledgePoint();
            kp.setCourse(course);
            kp.setName((String) pointData.get("name"));
            kp.setDescription((String) pointData.get("description"));
            kp.setDifficultyLevel(pointData.containsKey("difficultyLevel")
                    ? ((Number) pointData.get("difficultyLevel")).intValue() : 1);
            kp.setSortOrder(pointData.containsKey("sortOrder")
                    ? ((Number) pointData.get("sortOrder")).intValue() : pointCount);
            kp = knowledgePointRepository.save(kp);

            if (pointData.containsKey("tempId")) {
                idMap.put(((Number) pointData.get("tempId")).longValue(), kp);
            }
            pointCount++;
        }

        // 导入依赖关系
        int depCount = 0;
        if (dependencies != null) {
            for (Map<String, Object> depData : dependencies) {
                Long sourceId = ((Number) depData.get("sourceId")).longValue();
                Long targetId = ((Number) depData.get("targetId")).longValue();

                KnowledgePoint source = idMap.containsKey(sourceId)
                        ? idMap.get(sourceId)
                        : knowledgePointRepository.findById(sourceId).orElse(null);
                KnowledgePoint target = idMap.containsKey(targetId)
                        ? idMap.get(targetId)
                        : knowledgePointRepository.findById(targetId).orElse(null);

                if (source != null && target != null) {
                    KnowledgeDependency dep = new KnowledgeDependency();
                    dep.setSource(source);
                    dep.setTarget(target);
                    dep.setDependencyType(depData.containsKey("type")
                            ? (String) depData.get("type") : "REQUIRED");
                    dependencyRepository.save(dep);
                    depCount++;
                }
            }
        }

        return Map.of(
                "courseId", courseId,
                "importedPoints", pointCount,
                "importedDependencies", depCount
        );
    }

    public Map<String, Object> getKnowledgeGraph(Long courseId) {
        List<KnowledgePoint> points = knowledgePointRepository.findByCourseId(courseId);
        List<KnowledgeDependency> dependencies = dependencyRepository.findByCourseId(courseId);

        List<Map<String, Object>> nodes = points.stream().map(kp -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", kp.getId());
            node.put("name", kp.getName());
            node.put("description", kp.getDescription());
            node.put("difficultyLevel", kp.getDifficultyLevel());
            return node;
        }).collect(Collectors.toList());

        List<Map<String, Object>> edges = dependencies.stream().map(dep -> {
            Map<String, Object> edge = new HashMap<>();
            edge.put("source", dep.getSource().getId());
            edge.put("target", dep.getTarget().getId());
            edge.put("type", dep.getDependencyType());
            return edge;
        }).collect(Collectors.toList());

        return Map.of("nodes", nodes, "edges", edges);
    }
}
