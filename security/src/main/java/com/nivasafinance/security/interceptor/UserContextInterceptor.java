package com.nivasafinance.security.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nivasafinance.security.context.UserContext;
import com.nivasafinance.security.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserContextInterceptor.class);
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith(BEARER_PREFIX)) {
            logger.warn("Missing or invalid Authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing or invalid token");
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            DecodedJWT decodedJWT = JWT.decode(token);

            // Extract userId - throw exception if not found
            String userId = decodedJWT.getClaim("sub").asString();
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("Invalid token: userId is missing");
            }

            // Extract username - throw exception if not found
            String username = extractUsername(decodedJWT);
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Invalid token: username is missing");
            }

            String email = decodedJWT.getClaim("email").asString();
            String phoneNumber = decodedJWT.getClaim("phone_number").asString();

            UserInfo userInfo = new UserInfo(userId, username, email, phoneNumber);
            UserContext.setUserInfo(userInfo);
            logger.info("UserContext set successfully for user: {}", userInfo);

            return true;
        } catch (JWTDecodeException e) {
            logger.warn("Invalid JWT token", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid token");
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid token claims: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during token validation", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal server error");
            return false;
        }
    }

    private String extractUsername(DecodedJWT decodedJWT) {
        try {
            // Try to get username from user_metadata first
            var userMetadataClaim = decodedJWT.getClaim("user_metadata");
            if (!userMetadataClaim.isNull()) {
                Map<String, Object> userMetadata = userMetadataClaim.asMap();
                if (userMetadata != null) {
                    Object usernameObj = userMetadata.get("username");
                    if (usernameObj != null) {
                        String username = usernameObj.toString();
                        if (!username.isBlank()) {
                            return username;
                        }
                    }
                }
            }

            // Fallback to direct username claim
            String usernameClaim = decodedJWT.getClaim("username").asString();
            if (usernameClaim != null && !usernameClaim.isBlank()) {
                return usernameClaim;
            }

            // If neither found, return null (will trigger exception)
            return null;
        } catch (Exception ex) {
            logger.warn("Error extracting username: {}", ex.getMessage());
            return null;
        }
    }

    // Optionally handle logic after the controller has executed, before view rendering
    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) throws Exception {
        // No logic needed for postHandle, method exists for completeness.
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) throws Exception {
        // Clear ThreadLocal to avoid memory leaks
        UserContext.clear();
        logger.debug("UserContext cleared after request completion");
    }
}

