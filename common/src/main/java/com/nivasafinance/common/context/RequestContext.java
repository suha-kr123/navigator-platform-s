package com.nivasafinance.common.context;

import com.nivasafinance.common.dto.RequestMetadata;

public class RequestContext {

    private static final ThreadLocal<RequestMetadata> requestMetadataThreadLocal = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setRequestMetadata(RequestMetadata metadata) {
        requestMetadataThreadLocal.set(metadata);
    }

    public static RequestMetadata getRequestMetadata() {
        return requestMetadataThreadLocal.get();
    }

    public static void clear() {
        requestMetadataThreadLocal.remove();
    }
}
