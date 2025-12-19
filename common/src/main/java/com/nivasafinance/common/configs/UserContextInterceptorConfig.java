package com.nivasafinance.common.configs;

import com.nivasafinance.common.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UserContextInterceptorConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    public UserContextInterceptorConfig(UserContextInterceptor userContextInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")  // Apply to all paths
                .excludePathPatterns(
                        "/actuator/**",   // Exclude health check endpoints
                        "/error",         // Exclude error endpoint
                        "/ws/**"          // Exclude WebSocket endpoints (handled by WebSocketHandshakeHandler)
                );
    }
}