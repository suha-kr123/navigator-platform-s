package configs

import base.interceptor.UserContextInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor

@Configuration
class WebConfig : WebMvcConfigurer {

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
        // registry.addInterceptor(userContextInterceptor()) this
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200", "http://localhost:3000", "http://localhost:8080")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
    }
}
