package com.nivasafinance.common.utils;

import feign.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j(topic = "FeignResponseUtils")
public class FeignResponseUtils {

    public static String getContentTypeFromResponse(Response response) {
        Collection<String> contentTypeHeaders = response.headers().get("Content-Type");
        if (contentTypeHeaders != null && !contentTypeHeaders.isEmpty()) {
            return contentTypeHeaders.iterator().next();
        }
        return null;
    }

    public static Long getContentLengthFromResponse(Response response) {
        Collection<String> contentLengthHeaders = response.headers().get("Content-Length");
        if (contentLengthHeaders != null && !contentLengthHeaders.isEmpty()) {
            try {
                return Long.parseLong(contentLengthHeaders.iterator().next());
            } catch (NumberFormatException e) {
                log.warn("Invalid Content-Length header, will calculate from stream");
            }
        }
        return null;
    }
}
