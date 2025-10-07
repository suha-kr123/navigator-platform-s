package com.nivasafinance.common.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor

@Configuration
class WebConfig : WebMvcConfigurer {

    @Value("\${cors.origins}")
    private lateinit var corsOrigins: String

    @Bean
    fun localeChangeInterceptor(): LocaleChangeInterceptor =
        LocaleChangeInterceptor().apply {
            paramName = "lang"
        }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeChangeInterceptor())
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(*corsOrigins.split(",").toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
    }
}
