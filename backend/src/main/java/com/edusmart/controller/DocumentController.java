package com.edusmart.controller;

import com.edusmart.common.Result;
import com.edusmart.entity.UserDocument;
import com.edusmart.entity.User;
import com.edusmart.enums.DocumentType;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "个人知识库模块")
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    @Operation(summary = "上传文档")
    @PostMapping("/upload")
    public Result<UserDocument> upload(Authentication authentication,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) Long courseId) {
        Long userId = getUserId(authentication);
        return Result.success(documentService.uploadDocument(userId, file, courseId));
    }

    @Operation(summary = "我的文档列表")
    @GetMapping("/list")
    public Result<Page<UserDocument>> listDocuments(
            Authentication authentication,
            @RequestParam(required = false) DocumentType fileType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(authentication);
        return Result.success(documentService.getUserDocuments(userId, fileType, page, size));
    }

    @Operation(summary = "文档详情")
    @GetMapping("/{id}")
    public Result<UserDocument> getDocument(@PathVariable Long id) {
        return Result.success(documentService.getDocument(id));
    }

    @Operation(summary = "文档处理状态")
    @GetMapping("/{id}/status")
    public Result<Map<String, Object>> getDocumentStatus(@PathVariable Long id) {
        return Result.success(documentService.getDocumentStatus(id));
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return user.getId();
    }
}
