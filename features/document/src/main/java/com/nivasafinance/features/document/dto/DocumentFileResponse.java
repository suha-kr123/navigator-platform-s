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
public class DocumentFileResponse {
    private InputStream file;
    private DocumentResponse data;
}


