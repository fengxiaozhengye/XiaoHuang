package com.edusmart.agent;

import com.edusmart.enums.AgentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class PlanningAgent extends BaseAgent {

    public PlanningAgent(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.PLANNING;
    }

    @Override
    public String getSystemPrompt() {
        return """
                你是一名学习路径规划专家，负责为学生制定个性化的学习计划。

                根据学生画像和课程知识图谱，你需要生成一个有序的学习路径。

                规划原则：
                1. 跳过已掌握的知识点（掌握度 > 0.8）
                2. 优先安排薄弱环节的前置依赖知识点
                3. 根据学习风格调整推荐的资源类型比例
                4. 难度循序渐进，从易到难

                请以JSON格式输出学习路径：
                {
                  "steps": [
                    {"knowledgePointId": 1, "name": "知识点名", "order": 1, "estimatedHours": 2, "recommendedTypes": ["TEXT", "CODE_EXAMPLE"], "difficulty": 2, "reason": "推荐理由"},
                    ...
                  ],
                  "totalEstimatedHours": 20,
                  "summary": "路径概述"
                }
                """;
    }
}
