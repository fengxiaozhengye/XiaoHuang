package com.edusmart.service;

import com.edusmart.entity.UserDocument;
import com.edusmart.enums.DocumentStatus;
import com.edusmart.enums.DocumentType;
import com.edusmart.parser.PdfParser;
import com.edusmart.parser.TextChunker;
import com.edusmart.parser.WordParser;
import com.edusmart.repository.UserDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessService {

    private final UserDocumentRepository documentRepository;
    private final PdfParser pdfParser;
    private final WordParser wordParser;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;
    @Autowired(required = false)
    @Qualifier("pgVectorJdbcTemplate")
    private JdbcTemplate pgJdbcTemplate;

    @Async
    public void processDocument(Long documentId, MultipartFile file, DocumentType fileType, Long courseId) {
        UserDocument doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return;

        try {
            // Step 1: 解析内容
            updateStatus(doc, DocumentStatus.PARSING);
            String text = extractContent(file, fileType);
            if (text == null || text.isBlank()) {
                updateStatus(doc, DocumentStatus.FAILED, "无法提取文件内容");
                return;
            }

            // Step 2: 文本分块
            List<TextChunker.TextChunk> chunks = textChunker.chunk(text,
                    Map.of("source", doc.getFileName(), "fileType", fileType.name()));
            doc.setTotalChunks(chunks.size());
            documentRepository.save(doc);

            // Step 3: 向量化
            updateStatus(doc, DocumentStatus.VECTORIZING);
            if (pgJdbcTemplate == null) {
                log.warn("向量数据库不可用，跳过向量化，文档仅保存元数据");
                updateStatus(doc, DocumentStatus.COMPLETED);
                return;
            }
            for (TextChunker.TextChunk chunk : chunks) {
                try {
                    float[] embedding = embeddingService.embed(chunk.getContent());
                    String vectorStr = embeddingToPgVector(embedding);

                    String sql = """
                            INSERT INTO document_embedding (user_id, course_id, document_id, content, metadata, embedding)
                            VALUES (?, ?, ?, ?, ?::jsonb, ?::vector)
                            """;
                    String metadata = String.format("{\"source\": \"%s\", \"chunkIndex\": %d}",
                            doc.getFileName(), chunk.getIndex());
                    pgJdbcTemplate.update(sql,
                            doc.getUser().getId(), courseId, documentId,
                            chunk.getContent(), metadata, vectorStr);
                } catch (Exception e) {
                    log.warn("分块向量化失败: {}", e.getMessage());
                }
            }

            // Step 4: 完成
            updateStatus(doc, DocumentStatus.COMPLETED);
            log.info("文档处理完成: {}, 分块数: {}", doc.getFileName(), chunks.size());

        } catch (Exception e) {
            log.error("文档处理失败: {}", doc.getFileName(), e);
            updateStatus(doc, DocumentStatus.FAILED, e.getMessage());
        }
    }

    private String extractContent(MultipartFile file, DocumentType fileType) {
        try {
            return switch (fileType) {
                case PDF -> pdfParser.extract(file);
                case DOCX -> wordParser.extract(file);
                case TXT, MD -> new String(file.getBytes());
                case IMAGE -> null; // OCR需要额外配置
            };
        } catch (Exception e) {
            log.error("内容提取失败", e);
            return null;
        }
    }

    public void deleteDocumentEmbeddings(Long documentId) {
        try {
            pgJdbcTemplate.update("DELETE FROM document_embedding WHERE document_id = ?", documentId);
        } catch (Exception e) {
            log.warn("删除向量数据失败: {}", e.getMessage());
        }
    }

    private void updateStatus(UserDocument doc, DocumentStatus status) {
        updateStatus(doc, status, null);
    }

    private void updateStatus(UserDocument doc, DocumentStatus status, String errorMessage) {
        doc.setStatus(status);
        doc.setErrorMessage(errorMessage);
        documentRepository.save(doc);
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
