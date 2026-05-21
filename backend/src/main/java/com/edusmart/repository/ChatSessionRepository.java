package com.edusmart.repository;

import com.edusmart.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ChatSession> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
