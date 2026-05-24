package com.edusmart.service;

import com.edusmart.agent.Orchestrator;
import com.edusmart.agent.tool.RagRetrievalTool;
import com.edusmart.common.BusinessException;
import com.edusmart.entity.ChatMessage;
import com.edusmart.entity.ChatSession;
import com.edusmart.entity.User;
import com.edusmart.enums.AgentType;
import com.edusmart.repository.ChatMessageRepository;
import com.edusmart.repository.ChatSessionRepository;
import com.edusmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final Orchestrator orchestrator;
    private final EmbeddingService embeddingService;
    private final RagRetrievalTool ragRetrievalTool;

    public ChatSession createSession(Long userId, String agentType, Long courseId, Long knowledgePointId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setAgentType(agentType);
        session.setStatus("ACTIVE");
        if (courseId != null) {
            session.setCourse(new com.edusmart.entity.Course());
            session.getCourse().setId(courseId);
        }
        if (knowledgePointId != null) {
            session.setKnowledgePoint(new com.edusmart.entity.KnowledgePoint());
            session.getKnowledgePoint().setId(knowledgePointId);
        }
        return sessionRepository.save(session);
    }

    public String sendMessage(Long sessionId, Long userId, String content) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        // 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setRole("USER");
        userMsg.setContent(content);
        messageRepository.save(userMsg);

        // 构建上下文
        String context = buildContext(session, userId, content);

        // 调用Agent
        AgentType agentType = AgentType.valueOf(session.getAgentType());
        String response = orchestrator.dispatch(agentType, content, context);

        // 保存AI回复
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSession(session);
        aiMsg.setRole("ASSISTANT");
        aiMsg.setContent(response);
        messageRepository.save(aiMsg);

        return response;
    }

    public List<ChatSession> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<ChatMessage> getSessionMessages(Long sessionId, int page, int size) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, PageRequest.of(page, size));
    }

    private String buildContext(ChatSession session, Long userId, String query) {
        List<String> contextParts = new ArrayList<>();

        // 1. 加载最近对话历史
        Page<ChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(
                session.getId(), PageRequest.of(0, 10));
        if (!history.isEmpty()) {
            StringBuilder historyText = new StringBuilder("【对话历史】\n");
            for (ChatMessage msg : history.getContent()) {
                historyText.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
            contextParts.add(historyText.toString());
        }

        // 2. RAG检索（如果有Embedding服务可用）
        try {
            if (embeddingService.isAvailable()) {
                float[] queryEmbedding = embeddingService.embed(query);
                if (session.getCourse() != null) {
                    List<String> courseDocs = ragRetrievalTool.retrieveFromCourse(
                            session.getCourse().getId(), queryEmbedding, 3);
                    if (!courseDocs.isEmpty()) {
                        contextParts.add("【教材参考】\n" + String.join("\n---\n", courseDocs));
                    }
                }
                // 个人知识库检索
                List<String> personalDocs = ragRetrievalTool.retrieveFromPersonal(userId, queryEmbedding, 3);
                if (!personalDocs.isEmpty()) {
                    contextParts.add("【个人笔记参考】\n" + String.join("\n---\n", personalDocs));
                }
            }
        } catch (Exception e) {
            log.debug("RAG检索跳过: {}", e.getMessage());
        }

        return String.join("\n\n", contextParts);
    }
}
