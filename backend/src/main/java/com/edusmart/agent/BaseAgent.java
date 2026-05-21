package com.edusmart.agent;

import com.edusmart.enums.AgentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;

@Slf4j
public abstract class BaseAgent {

    protected final ChatClient chatClient;

    protected BaseAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public abstract AgentType getAgentType();

    public abstract String getSystemPrompt();

    /**
     * 同步调用Agent
     */
    public String chat(String userMessage, String contextPrompt) {
        log.info("[{}] 收到请求，上下文长度: {}", getAgentType(), contextPrompt != null ? contextPrompt.length() : 0);

        String fullPrompt = buildFullPrompt(userMessage, contextPrompt);

        ChatResponse response = chatClient.prompt()
                .system(getSystemPrompt())
                .user(fullPrompt)
                .call()
                .chatResponse();

        String result = response.getResult().getOutput().getText();
        log.info("[{}] 响应完成，长度: {}", getAgentType(), result != null ? result.length() : 0);
        return result;
    }

    /**
     * 流式调用Agent
     */
    public org.springframework.ai.chat.model.ChatResponse chatStream(String userMessage, String contextPrompt) {
        String fullPrompt = buildFullPrompt(userMessage, contextPrompt);
        return chatClient.prompt()
                .system(getSystemPrompt())
                .user(fullPrompt)
                .call()
                .chatResponse();
    }

    protected String buildFullPrompt(String userMessage, String contextPrompt) {
        if (contextPrompt == null || contextPrompt.isBlank()) {
            return userMessage;
        }
        return "【上下文信息】\n" + contextPrompt + "\n\n【用户请求】\n" + userMessage;
    }
}
