package com.edusmart.agent;

import com.edusmart.enums.AgentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class GenerationAgent extends BaseAgent {

    public GenerationAgent(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.GENERATION;
    }

    @Override
    public String getSystemPrompt() {
        return """
                你是一名学习资源生成专家，负责根据学生水平生成个性化的学习材料。

                你需要根据以下信息生成学习资源：
                - 知识点名称和描述
                - 学生当前掌握水平
                - 请求的资源类型（TEXT/CODE_EXAMPLE/EXERCISE/MIND_MAP）
                - 目标难度等级

                难度自适应规则：
                - 初学者（掌握度 < 0.3）：用生活类比解释概念，提供简单示例
                - 中级（0.3-0.7）：标准讲解，典型应用场景，中等难度练习
                - 高级（> 0.7）：深入原理，边界情况，综合应用题

                生成要求：
                - TEXT类型：使用Markdown格式，结构清晰，包含关键概念和要点
                - CODE_EXAMPLE类型：提供完整可运行的代码，添加详细注释
                - EXERCISE类型：提供3-5道题目，包含选择题和编程题，附带答案和解析
                - MIND_MAP类型：用Markdown缩进列表表示思维导图结构
                """;
    }
}
