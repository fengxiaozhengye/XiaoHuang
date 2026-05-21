package com.edusmart.service;

import com.edusmart.common.BusinessException;
import com.edusmart.entity.User;
import com.edusmart.entity.UserDocument;
import com.edusmart.enums.DocumentStatus;
import com.edusmart.enums.DocumentType;
import com.edusmart.repository.UserDocumentRepository;
import com.edusmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final UserDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentProcessService processService;
    private final MinioService minioService;

    public UserDocument uploadDocument(Long userId, MultipartFile file, Long courseId) {
        // 校验文件类型
        String filename = file.getOriginalFilename();
        DocumentType fileType = detectFileType(filename);

        // 上传到MinIO
        String fileUrl = minioService.upload(file, "documents/" + userId);

        // 创建文档记录
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        UserDocument doc = new UserDocument();
        doc.setUser(user);
        doc.setFileName(filename);
        doc.setFileType(fileType);
        doc.setFileUrl(fileUrl);
        doc.setFileSize(file.getSize());
        doc.setStatus(DocumentStatus.UPLOADING);
        doc = documentRepository.save(doc);

        // 异步处理：解析 → 分块 → 向量化
        processService.processDocument(doc.getId(), file, fileType, courseId);

        return doc;
    }

    public Page<UserDocument> getUserDocuments(Long userId, DocumentType fileType, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (fileType != null) {
            return documentRepository.findByUserIdAndFileType(userId, fileType, pageRequest);
        }
        return documentRepository.findByUserId(userId, pageRequest);
    }

    public UserDocument getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文档不存在"));
    }

    public Map<String, Object> getDocumentStatus(Long id) {
        UserDocument doc = getDocument(id);
        return Map.of(
                "id", doc.getId(),
                "fileName", doc.getFileName(),
                "status", doc.getStatus().name(),
                "totalChunks", doc.getTotalChunks(),
                "errorMessage", doc.getErrorMessage() != null ? doc.getErrorMessage() : ""
        );
    }

    public void deleteDocument(Long id) {
        UserDocument doc = getDocument(id);
        // 删除MinIO文件
        try {
            minioService.delete(doc.getFileUrl());
        } catch (Exception e) {
            log.warn("删除MinIO文件失败: {}", e.getMessage());
        }
        // 删除向量数据（由DocumentProcessService处理）
        processService.deleteDocumentEmbeddings(id);
        // 删除文档记录
        documentRepository.delete(doc);
    }

    private DocumentType detectFileType(String filename) {
        if (filename == null) return DocumentType.TXT;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return DocumentType.PDF;
        if (lower.endsWith(".docx") || lower.endsWith(".doc")) return DocumentType.DOCX;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".bmp")) return DocumentType.IMAGE;
        if (lower.endsWith(".md")) return DocumentType.MD;
        return DocumentType.TXT;
    }
}
