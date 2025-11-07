package com.nivasafinance.common.interceptor;

import com.nivasafinance.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to extract username from request header and set it in UserContext.
 * The API Gateway forwards the username in the X-Username header after JWT validation.
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserContextInterceptor.class);
    private static final String USERNAME_HEADER = "X-Username";
    private static final String SYSTEM_USERNAME = "system";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String username = request.getHeader(USERNAME_HEADER);

        if (username == null || username.isBlank()) {
            logger.warn("Missing or empty {} header in request: {} {}",
                    USERNAME_HEADER, request.getMethod(), request.getRequestURI());
            // Set default username for requests without authentication
            username = SYSTEM_USERNAME;
        }

        UserContext.setUsername(username);
        logger.info("UserContext set for username: {}", username);

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) throws Exception {
        // Clear ThreadLocal to avoid memory leaks
        UserContext.clear();
        logger.info("UserContext cleared after request completion");
    }
}