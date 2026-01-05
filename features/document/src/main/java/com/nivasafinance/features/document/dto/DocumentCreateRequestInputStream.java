package com.nivasafinance.features.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCreateRequestInputStream {
    private String name;
    private InputStream file;
    private String customPath;
    private String contentType;
    private Long size;
}

