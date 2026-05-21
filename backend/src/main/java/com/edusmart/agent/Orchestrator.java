package com.edusmart.agent;

import com.edusmart.enums.AgentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class Orchestrator {

    private final DiagnosisAgent diagnosisAgent;
    private final PlanningAgent planningAgent;
    private final GenerationAgent generationAgent;
    private final TutorAgent tutorAgent;
    private final EvaluationAgent evaluationAgent;

    private Map<AgentType, BaseAgent> agentMap;

    private Map<AgentType, BaseAgent> getAgentMap() {
        if (agentMap == null) {
            agentMap = new EnumMap<>(AgentType.class);
            agentMap.put(AgentType.DIAGNOSIS, diagnosisAgent);
            agentMap.put(AgentType.PLANNING, planningAgent);
            agentMap.put(AgentType.GENERATION, generationAgent);
            agentMap.put(AgentType.TUTOR, tutorAgent);
            agentMap.put(AgentType.EVALUATION, evaluationAgent);
        }
        return agentMap;
    }

    /**
     * 调用指定Agent
     */
    public String dispatch(AgentType agentType, String userMessage, String contextPrompt) {
        BaseAgent agent = getAgentMap().get(agentType);
        if (agent == null) {
            throw new IllegalArgumentException("未知的Agent类型: " + agentType);
        }
        log.info("调度Agent: {}", agentType);
        return agent.chat(userMessage, contextPrompt);
    }

    /**
     * 串行编排：诊断 → 规划
     * 用于新学生首次学习流程
     */
    public Map<AgentType, String> diagnoseAndPlan(String assessData, String courseKnowledgeGraph) {
        log.info("启动诊断→规划编排");

        // Step 1: 诊断
        String diagnosisResult = diagnosisAgent.chat(assessData, null);
        log.info("诊断完成");

        // Step 2: 规划（以诊断结果为上下文）
        String planResult = planningAgent.chat(
                "请根据以上学生画像，为该课程生成个性化学习路径",
                "【学生画像】\n" + diagnosisResult + "\n\n【课程知识图谱】\n" + courseKnowledgeGraph
        );
        log.info("规划完成");

        return Map.of(
                AgentType.DIAGNOSIS, diagnosisResult,
                AgentType.PLANNING, planResult
        );
    }

    /**
     * 获取Agent实例（用于需要直接操作的场景）
     */
    public BaseAgent getAgent(AgentType agentType) {
        return getAgentMap().get(agentType);
    }
}
