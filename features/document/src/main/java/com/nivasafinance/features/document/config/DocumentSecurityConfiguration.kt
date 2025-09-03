package com.nivasafinance.features.document.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "document.security")
data class DocumentSecurityConfiguration(
    var enablePathTraversalProtection: Boolean = true,
    var enableSuspiciousPatternDetection: Boolean = true,
    var maxStorageKeyLength: Int = 255,
    var maxFileNameLength: Int = 255
)
