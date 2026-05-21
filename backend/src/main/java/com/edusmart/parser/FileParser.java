package com.edusmart.parser;

import org.springframework.web.multipart.MultipartFile;

public interface FileParser {
    String extract(MultipartFile file) throws Exception;
}
