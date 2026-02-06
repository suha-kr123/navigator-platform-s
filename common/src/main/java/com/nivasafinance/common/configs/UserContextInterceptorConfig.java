package com.nivasafinance.common.configs;

import com.nivasafinance.common.interceptor.RequestContextInterceptor;
import com.nivasafinance.common.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UserContextInterceptorConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;
    private final RequestContextInterceptor requestContextInterceptor;

    public UserContextInterceptorConfig(UserContextInterceptor userContextInterceptor, RequestContextInterceptor requestContextInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
        this.requestContextInterceptor = requestContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")  // Apply to all paths
                .excludePathPatterns(
                        "/actuator/**",   // Exclude health check endpoints
                        "/error"          // Exclude error endpoint
                );

        registry.addInterceptor(requestContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/error",
                        "/ws/**"
                );
    }
}