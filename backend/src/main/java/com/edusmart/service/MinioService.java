package com.edusmart.service;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

@Slf4j
@Service
public class MinioService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // 确保bucket存在
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("创建MinIO bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("MinIO初始化失败（非致命，文件上传功能不可用）: {}", e.getMessage());
        }
    }

    public String upload(MultipartFile file, String prefix) {
        if (minioClient == null) {
            throw new RuntimeException("MinIO未配置或连接失败");
        }
        try {
            String objectName = prefix + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    public void delete(String fileUrl) {
        if (minioClient == null || fileUrl == null) return;
        try {
            String objectName = fileUrl.replace(endpoint + "/" + bucketName + "/", "");
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.warn("文件删除失败: {}", e.getMessage());
        }
    }
}
