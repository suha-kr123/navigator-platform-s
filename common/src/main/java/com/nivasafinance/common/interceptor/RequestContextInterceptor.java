package com.nivasafinance.common.interceptor;

import com.nivasafinance.common.context.RequestContext;
import com.nivasafinance.common.dto.RequestMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestContextInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestContextInterceptor.class);

    public static final String HEADER_AUDIT_ID = "X-Audit-Id";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        RequestMetadata.RequestMetadataBuilder builder = RequestMetadata.builder();

        String auditIdHeader = request.getHeader(HEADER_AUDIT_ID);
        if(auditIdHeader != null && !auditIdHeader.isBlank()) {
            try{
                builder.auditId(Long.parseLong(auditIdHeader.trim()));
            } catch (NumberFormatException e) {
                logger.warn("Invalid {} header value: {}", HEADER_AUDIT_ID, auditIdHeader);
            }
        }
        RequestContext.setRequestMetadata(builder.build());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex){
        RequestContext.clear();
        logger.debug("RequestContext cleared after request completion");
    }
}
