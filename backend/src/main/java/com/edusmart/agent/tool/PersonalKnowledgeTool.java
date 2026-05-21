package com.edusmart.agent.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PersonalKnowledgeTool {

    @Autowired(required = false)
    @Qualifier("pgVectorJdbcTemplate")
    private JdbcTemplate pgJdbcTemplate;

    private final EmbeddingServiceProxy embeddingServiceProxy;

    public PersonalKnowledgeTool(EmbeddingServiceProxy embeddingServiceProxy) {
        this.embeddingServiceProxy = embeddingServiceProxy;
    }

    /**
     * 检索个人知识库
     */
    public List<Map<String, String>> search(Long userId, String query, int topK) {
        if (pgJdbcTemplate == null) {
            log.warn("向量数据库不可用，跳过个人知识库检索");
            return List.of();
        }
        try {
            if (!embeddingServiceProxy.isAvailable()) {
                return List.of();
            }
            float[] queryEmbedding = embeddingServiceProxy.embed(query);
            String vectorStr = embeddingToPgVector(queryEmbedding);

            String sql = """
                    SELECT content,
                           1 - (embedding <=> ?::vector) AS similarity,
                           metadata->>'source' AS source
                    FROM document_embedding
                    WHERE user_id = ?
                    ORDER BY embedding <=> ?::vector
                    LIMIT ?
                    """;

            List<Map<String, Object>> results = pgJdbcTemplate.queryForList(sql, vectorStr, userId, vectorStr, topK);
            return results.stream()
                    .map(r -> Map.of(
                            "content", (String) r.get("content"),
                            "similarity", String.valueOf(r.get("similarity")),
                            "source", r.get("source") != null ? (String) r.get("source") : "个人文档"
                    ))
                    .toList();
        } catch (Exception e) {
            log.warn("个人知识库检索失败: {}", e.getMessage());
            return List.of();
        }
    }

    private String embeddingToPgVector(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 代理类，避免循环依赖
     */
    @Component
    @RequiredArgsConstructor
    public static class EmbeddingServiceProxy {
        private final com.edusmart.service.EmbeddingService embeddingService;

        public boolean isAvailable() {
            return embeddingService.isAvailable();
        }

        public float[] embed(String text) {
            return embeddingService.embed(text);
        }
    }
}
