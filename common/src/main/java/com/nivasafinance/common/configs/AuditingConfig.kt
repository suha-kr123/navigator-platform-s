package com.nivasafinance.common.configs

import com.nivasafinance.common.audit.AuditorAwareImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class AuditingConfig {

    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AuditorAwareImpl()
    }
}
