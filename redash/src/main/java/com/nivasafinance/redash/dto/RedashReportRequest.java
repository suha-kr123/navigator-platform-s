package com.nivasafinance.redash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RedashReportRequest {
    private Long queryId;
    private FileType fileType;
    private Map<String, Object> parameters;
}
