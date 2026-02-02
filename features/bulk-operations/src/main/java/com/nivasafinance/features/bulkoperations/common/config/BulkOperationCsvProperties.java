package com.nivasafinance.features.bulkoperations.common.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "bulkoperations.csv")
public class BulkOperationCsvProperties {

    /** Max CSV file size in bytes; default 10MB if not set (e.g. when properties file not loaded). */
    private long maxFileSize = 10 * 1024 * 1024L;

    /** Max number of data rows allowed per file; prevents unbounded memory use. */
    private int maxRows = 50_000;

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = Math.max(1, maxRows);
    }
}

