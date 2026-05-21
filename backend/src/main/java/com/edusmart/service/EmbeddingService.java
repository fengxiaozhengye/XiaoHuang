package com.edusmart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmbeddingService {

    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    public boolean isAvailable() {
        return embeddingModel != null;
    }

    /**
     * 生成文本的向量嵌入
     */
    public float[] embed(String text) {
        if (embeddingModel == null) {
            throw new IllegalStateException("EmbeddingModel未配置");
        }
        float[] result = embeddingModel.embed(text);
        log.debug("生成向量嵌入，维度: {}", result.length);
        return result;
    }

    /**
     * 批量生成向量嵌入
     */
    public List<float[]> embedBatch(List<String> texts) {
        if (embeddingModel == null) {
            throw new IllegalStateException("EmbeddingModel未配置");
        }
        return embeddingModel.embed(texts).stream()
                .map(embedding -> {
                    float[] arr = new float[embedding.length];
                    for (int i = 0; i < embedding.length; i++) {
                        arr[i] = (float) embedding[i];
                    }
                    return arr;
                })
                .toList();
    }
}
