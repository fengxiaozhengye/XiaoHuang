package com.edusmart.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@Slf4j
@Component
public class WordParser implements FileParser {

    @Override
    public String extract(MultipartFile file) throws Exception {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            String text = document.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .filter(t -> !t.isBlank())
                    .collect(Collectors.joining("\n\n"));
            log.debug("Word解析完成，段落数: {}, 字符数: {}", document.getParagraphs().size(), text.length());
            return text;
        }
    }
}
