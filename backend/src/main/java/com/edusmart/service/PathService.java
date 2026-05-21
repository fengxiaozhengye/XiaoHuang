package com.edusmart.service;

import com.edusmart.agent.Orchestrator;
import com.edusmart.agent.tool.KnowledgeGraphTool;
import com.edusmart.common.BusinessException;
import com.edusmart.entity.LearningPath;
import com.edusmart.entity.User;
import com.edusmart.repository.CourseRepository;
import com.edusmart.repository.LearningPathRepository;
import com.edusmart.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathService {

    private final LearningPathRepository pathRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final Orchestrator orchestrator;
    private final KnowledgeGraphTool knowledgeGraphTool;
    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    public LearningPath generatePath(Long userId, Long courseId) {
        // 获取学生画像
        String profileJson = "";
        var profile = profileService.getProfile(userId);
        if (profile != null) {
            profileJson = "学习风格: " + (profile.getLearningStyle() != null ? profile.getLearningStyle().name() : "未知") +
                    "\n掌握度: " + (profile.getKnowledgeLevel() != null ? profile.getKnowledgeLevel() : "{}") +
                    "\n薄弱点: " + (profile.getWeakPoints() != null ? profile.getWeakPoints() : "[]") +
                    "\n优势: " + (profile.getStrongPoints() != null ? profile.getStrongPoints() : "[]");
        }

        // 获取知识图谱
        String graphText = knowledgeGraphTool.getCourseKnowledgeGraphText(courseId);

        // 调用规划Agent
        String planResult = orchestrator.dispatch(
                com.edusmart.enums.AgentType.PLANNING,
                "请为该学生生成个性化学习路径",
                "【学生画像】\n" + profileJson + "\n\n【课程知识图谱】\n" + graphText
        );

        // 保存学习路径
        User user = userRepository.findById(userId).orElseThrow();
        LearningPath path = new LearningPath();
        path.setUser(user);
        path.setCourse(courseRepository.findById(courseId).orElseThrow());
        path.setStatus("ACTIVE");
        path.setPathData(planResult);

        // 尝试解析步骤数
        try {
            Map<String, Object> planMap = objectMapper.readValue(planResult, Map.class);
            if (planMap.containsKey("steps")) {
                List<?> steps = (List<?>) planMap.get("steps");
                path.setTotalSteps(steps.size());
            }
        } catch (Exception e) {
            log.warn("解析路径数据失败: {}", e.getMessage());
        }

        return pathRepository.save(path);
    }

    public LearningPath getActivePath(Long userId, Long courseId) {
        return pathRepository.findByUserIdAndCourseIdAndStatus(userId, courseId, "ACTIVE")
                .orElse(null);
    }

    public List<LearningPath> getUserPaths(Long userId) {
        return pathRepository.findByUserId(userId);
    }

    public LearningPath advanceStep(Long pathId) {
        LearningPath path = pathRepository.findById(pathId)
                .orElseThrow(() -> new BusinessException("学习路径不存在"));
        path.setCurrentStep(path.getCurrentStep() + 1);
        if (path.getCurrentStep() >= path.getTotalSteps()) {
            path.setStatus("COMPLETED");
        }
        return pathRepository.save(path);
    }
}
