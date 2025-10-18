package com.nivasafinance.security.config

import com.nivasafinance.security.interceptor.UserContextInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class InterceptorConfig : WebMvcConfigurer {

    @Bean
    fun userContextInterceptor(): UserContextInterceptor =
        UserContextInterceptor()

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(userContextInterceptor())
            .addPathPatterns("/**")
    }
}
