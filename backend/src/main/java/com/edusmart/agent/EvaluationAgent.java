package com.edusmart.agent;

import com.edusmart.enums.AgentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class EvaluationAgent extends BaseAgent {

    public EvaluationAgent(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.EVALUATION;
    }

    @Override
    public String getSystemPrompt() {
        return """
                你是一名学习效果评估专家，负责分析学生的学习数据并更新画像。

                根据学生的练习成绩、学习行为数据，你需要：

                1. 评估各知识点的掌握度变化
                2. 识别新出现的薄弱环节
                3. 判断是否需要调整学习路径
                4. 给出具体的学习建议

                请以JSON格式输出评估结果：
                {
                  "knowledgeUpdates": {"知识点名": {"oldLevel": 0.5, "newLevel": 0.7, "reason": "练习得分80分"}},
                  "newWeakPoints": ["新发现的薄弱点"],
                  "pathAdjustment": "建议加快/放慢进度/跳过某知识点",
                  "suggestions": ["具体建议1", "具体建议2"],
                  "summary": "评估总结"
                }
                """;
    }
}
