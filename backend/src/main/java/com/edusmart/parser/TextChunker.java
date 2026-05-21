package com.edusmart.parser;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TextChunker {

    private static final int MAX_CHUNK_SIZE = 512;
    private static final int OVERLAP_SIZE = 64;
    private static final Pattern PARAGRAPH_SPLIT = Pattern.compile("\n\n+");
    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[。！？.!?\\n])");

    @Data
    public static class TextChunk {
        private String content;
        private int index;
        private Map<String, Object> metadata;

        public TextChunk(String content, int index, Map<String, Object> metadata) {
            this.content = content;
            this.index = index;
            this.metadata = metadata;
        }
    }

    /**
     * 按语义分块
     */
    public List<TextChunk> chunk(String text, Map<String, Object> metadata) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        // 1. 按段落分割
        String[] paragraphs = PARAGRAPH_SPLIT.split(text);

        // 2. 合并小段落，拆分大段落
        List<String> rawChunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String para : paragraphs) {
            para = para.trim();
            if (para.isEmpty()) continue;

            if (current.length() + para.length() > MAX_CHUNK_SIZE) {
                if (current.length() > 0) {
                    rawChunks.add(current.toString().trim());
                    current = new StringBuilder();
                }
                // 超大段落按句子拆分
                if (para.length() > MAX_CHUNK_SIZE) {
                    rawChunks.addAll(splitBySentence(para));
                } else {
                    current.append(para).append("\n\n");
                }
            } else {
                current.append(para).append("\n\n");
            }
        }
        if (current.length() > 0) {
            rawChunks.add(current.toString().trim());
        }

        // 3. 创建TextChunk对象
        List<TextChunk> chunks = new ArrayList<>();
        for (int i = 0; i < rawChunks.size(); i++) {
            chunks.add(new TextChunk(rawChunks.get(i), i, metadata));
        }

        log.debug("文本分块完成，总字符: {}, 分块数: {}", text.length(), chunks.size());
        return chunks;
    }

    private List<String> splitBySentence(String text) {
        List<String> result = new ArrayList<>();
        String[] sentences = SENTENCE_SPLIT.split(text);
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;

            if (current.length() + sentence.length() > MAX_CHUNK_SIZE) {
                if (current.length() > 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                }
            }
            current.append(sentence);
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        return result;
    }
}
