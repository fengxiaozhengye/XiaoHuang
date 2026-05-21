package com.edusmart.repository;

import com.edusmart.entity.UserDocument;
import com.edusmart.enums.DocumentStatus;
import com.edusmart.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {

    Page<UserDocument> findByUserId(Long userId, Pageable pageable);

    Page<UserDocument> findByUserIdAndFileType(Long userId, DocumentType fileType, Pageable pageable);

    long countByUserIdAndStatus(Long userId, DocumentStatus status);
}
