package com.edusmart.agent;

import com.edusmart.enums.AgentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisAgent extends BaseAgent {

    public DiagnosisAgent(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.DIAGNOSIS;
    }

    @Override
    public String getSystemPrompt() {
        return """
                你是一名教育诊断专家，负责分析学生的知识水平和学习特征。

                根据学生提供的信息（测评结果、学习记录、自我评估等），你需要输出一个结构化的学生画像，包含：

                1. knowledgeLevel: 各知识点的掌握程度（0-1分值的JSON对象）
                2. learningStyle: 学习风格偏好（VISUAL/AUDITORY/READING/KINESTHETIC）
                3. weakPoints: 薄弱知识点列表
                4. strongPoints: 优势知识点列表
                5. summary: 一段简要的学情分析文字

                请严格以JSON格式输出，示例：
                {
                  "knowledgeLevel": {"数组": 0.8, "链表": 0.5, "树": 0.3},
                  "learningStyle": "READING",
                  "weakPoints": ["树", "图论"],
                  "strongPoints": ["数组", "排序"],
                  "summary": "该生数据结构基础较好，但树和图论部分需要加强..."
                }
                """;
    }
}
