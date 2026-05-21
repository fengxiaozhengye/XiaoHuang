package com.edusmart.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RagRetrievalTool {

    @Autowired(required = false)
    @Qualifier("pgVectorJdbcTemplate")
    private JdbcTemplate pgJdbcTemplate;

    /**
     * 检索课程教材中最相关的文本块
     */
    public List<String> retrieveFromCourse(Long courseId, float[] queryEmbedding, int topK) {
        if (pgJdbcTemplate == null) {
            log.warn("向量数据库不可用，跳过RAG检索");
            return List.of();
        }
        try {
            String vectorStr = embeddingToPgVector(queryEmbedding);
            String sql = """
                    SELECT content, 1 - (embedding <=> ?::vector) AS similarity
                    FROM document_embedding
                    WHERE course_id = ? AND user_id IS NULL
                    ORDER BY embedding <=> ?::vector
                    LIMIT ?
                    """;
            List<Map<String, Object>> results = pgJdbcTemplate.queryForList(sql, vectorStr, courseId, vectorStr, topK);
            return results.stream()
                    .map(r -> (String) r.get("content"))
                    .toList();
        } catch (Exception e) {
            log.warn("RAG检索失败（课程文档）: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 检索个人知识库中最相关的文本块
     */
    public List<String> retrieveFromPersonal(Long userId, float[] queryEmbedding, int topK) {
        if (pgJdbcTemplate == null) {
            log.warn("向量数据库不可用，跳过RAG检索");
            return List.of();
        }
        try {
            String vectorStr = embeddingToPgVector(queryEmbedding);
            String sql = """
                    SELECT content, 1 - (embedding <=> ?::vector) AS similarity,
                           metadata->>'source' AS source
                    FROM document_embedding
                    WHERE user_id = ?
                    ORDER BY embedding <=> ?::vector
                    LIMIT ?
                    """;
            List<Map<String, Object>> results = pgJdbcTemplate.queryForList(sql, vectorStr, userId, vectorStr, topK);
            return results.stream()
                    .map(r -> {
                        String source = (String) r.get("source");
                        String content = (String) r.get("content");
                        return "[来源: " + (source != null ? source : "个人文档") + "]\n" + content;
                    })
                    .toList();
        } catch (Exception e) {
            log.warn("RAG检索失败（个人知识库）: {}", e.getMessage());
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
}
