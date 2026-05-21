package com.edusmart.controller;

import com.edusmart.common.Result;
import com.edusmart.entity.ChatMessage;
import com.edusmart.entity.ChatSession;
import com.edusmart.entity.User;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Tag(name = "AI对话模块")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @Operation(summary = "创建对话会话")
    @PostMapping("/session")
    public Result<ChatSession> createSession(Authentication authentication,
                                              @RequestBody Map<String, Object> body) {
        Long userId = getUserId(authentication);
        String agentType = (String) body.getOrDefault("agentType", "TUTOR");
        Long courseId = body.containsKey("courseId") ? ((Number) body.get("courseId")).longValue() : null;
        Long kpId = body.containsKey("knowledgePointId") ? ((Number) body.get("knowledgePointId")).longValue() : null;
        return Result.success(chatService.createSession(userId, agentType, courseId, kpId));
    }

    @Operation(summary = "发送消息（SSE流式响应）")
    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(Authentication authentication, @RequestBody Map<String, Object> body) {
        Long sessionId = ((Number) body.get("sessionId")).longValue();
        String content = (String) body.get("content");
        Long userId = getUserId(authentication);

        SseEmitter emitter = new SseEmitter(60000L);

        CompletableFuture.runAsync(() -> {
            try {
                String response = chatService.sendMessage(sessionId, userId, content);
                // 分段发送（模拟流式）
                String[] words = response.split("(?<=。|！|？|\\n)");
                for (String word : words) {
                    emitter.send(SseEmitter.event()
                            .data(Map.of("type", "content", "content", word)));
                    Thread.sleep(50);
                }
                emitter.send(SseEmitter.event()
                        .data(Map.of("type", "done", "content", "")));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .data(Map.of("type", "error", "content", e.getMessage())));
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getSessions(Authentication authentication) {
        Long userId = getUserId(authentication);
        return Result.success(chatService.getUserSessions(userId));
    }

    @Operation(summary = "获取对话历史")
    @GetMapping("/session/{sessionId}/history")
    public Result<Page<ChatMessage>> getHistory(@PathVariable Long sessionId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        return Result.success(chatService.getSessionMessages(sessionId, page, size));
    }

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return user.getId();
    }
}
