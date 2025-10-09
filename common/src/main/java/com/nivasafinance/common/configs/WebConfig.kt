package com.nivasafinance.common.configs

import com.nivasafinance.common.base.interceptor.UserContextInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor

@Configuration
class WebConfig(
    @Value("\${cors.origins}") private val corsOrigins: String
) : WebMvcConfigurer {

    @Bean
    fun localeChangeInterceptor(): LocaleChangeInterceptor =
        LocaleChangeInterceptor().apply {
            paramName = "lang"
        }

    @Bean
    fun userContextInterceptor(): UserContextInterceptor =
        UserContextInterceptor()

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeChangeInterceptor())
        // Temporarily disabled for testing
        // registry.addInterceptor(userContextInterceptor())
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        val allowedOrigins = corsOrigins.split(",").map { it.trim() }.toTypedArray()
        registry.addMapping("/api/**")
            .allowedOriginPatterns(*allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
    }
}
