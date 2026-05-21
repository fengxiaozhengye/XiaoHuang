package com.edusmart.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是EduSmart智慧学习平台的AI助手，专注于高等教育个性化学习辅导。请用中文回答。")
                .build();
    }
}
