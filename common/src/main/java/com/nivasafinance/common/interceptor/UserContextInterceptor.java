package com.nivasafinance.common.interceptor;

import com.nivasafinance.common.constants.AuthConstants;
import com.nivasafinance.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to extract username from request header and set it in UserContext.
 * The API Gateway forwards the username in the X-Username header after JWT validation.
 * When X-Username is absent, falls back to resolved username from JWT (email/phone) via request attribute.
 */
@Component
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserContextInterceptor.class);
    private static final String USERNAME_HEADER = "X-Username";
    private static final String SYSTEM_USERNAME = "system";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        String username = request.getHeader(USERNAME_HEADER);
        if (username == null || username.isBlank()) {
            Object resolved = request.getAttribute(AuthConstants.RESOLVED_USERNAME_ATTRIBUTE);
            if (resolved instanceof String s && !s.isBlank()) {
                username = s;
            }
        }
        if (username == null || username.isBlank()) {
            logger.error("Missing mandatory {} header in request: {} {}",
                    USERNAME_HEADER, request.getMethod(), request.getRequestURI());
             username = SYSTEM_USERNAME;
        }

     //   userStatusValidator.validateActiveUser(username); //TODO

        UserContext.setUsername(username);
        logger.info("UserContext set for username: {}", username);

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        // Clear ThreadLocal to avoid memory leaks
        UserContext.clear();
        logger.info("UserContext cleared after request completion");
    }
}