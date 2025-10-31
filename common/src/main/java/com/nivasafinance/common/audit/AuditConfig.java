package com.nivasafinance.common.audit;

import org.springframework.web.util.pattern.PathPattern;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Data class to hold audit configuration for a method
 */
public class AuditConfig {
    private final boolean skipAudit;
    private final boolean ignoreResponse;
    private final List<String> uriPatterns;
    private final List<PathPattern> pathPatterns;

    public AuditConfig() {
        this(false, false, Collections.emptyList(), Collections.emptyList());
    }

    public AuditConfig(boolean skipAudit, boolean ignoreResponse, 
                       List<String> uriPatterns, List<PathPattern> pathPatterns) {
        this.skipAudit = skipAudit;
        this.ignoreResponse = ignoreResponse;
        this.uriPatterns = uriPatterns != null ? uriPatterns : Collections.emptyList();
        this.pathPatterns = pathPatterns != null ? pathPatterns : Collections.emptyList();
    }

    public boolean isSkipAudit() {
        return skipAudit;
    }

    public boolean isIgnoreResponse() {
        return ignoreResponse;
    }

    public List<String> getUriPatterns() {
        return uriPatterns;
    }

    public List<PathPattern> getPathPatterns() {
        return pathPatterns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditConfig that = (AuditConfig) o;
        return skipAudit == that.skipAudit &&
                ignoreResponse == that.ignoreResponse &&
                Objects.equals(uriPatterns, that.uriPatterns) &&
                Objects.equals(pathPatterns, that.pathPatterns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skipAudit, ignoreResponse, uriPatterns, pathPatterns);
    }

    @Override
    public String toString() {
        return "AuditConfig{" +
                "skipAudit=" + skipAudit +
                ", ignoreResponse=" + ignoreResponse +
                ", uriPatterns=" + uriPatterns +
                ", pathPatterns=" + pathPatterns +
                '}';
    }
}

