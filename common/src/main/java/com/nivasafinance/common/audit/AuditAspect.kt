package com.nivasafinance.common.audit

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import java.util.concurrent.ConcurrentHashMap

/**
 * AOP Aspect to handle @NonAuditable annotation and cache audit configuration
 * for efficient lookup in the ApiAuditFilter.
 */
@Aspect
@Component
class AuditAspect {

    companion object {
        // Cache to store audit configuration for each method
        // Key: "className.methodName" format
        // Value: AuditConfig containing the audit settings
        private val auditConfigCache = ConcurrentHashMap<String, AuditConfig>()

        // PathPattern parser for efficient URL matching
        private val pathPatternParser = PathPatternParser()

        /**
         * Get audit configuration for a specific method
         */
        fun getAuditConfig(className: String, methodName: String): AuditConfig? {
            val key = "$className.$methodName"
            return auditConfigCache[key]
        }

        /**
         * Get audit configuration for a specific method by URI pattern
         * This is used by the filter when we have URI but not exact method info
         */
        fun getAuditConfigByUri(uri: String): AuditConfig? {
            return auditConfigCache.values.find { config ->
                config.pathPatterns.any { pattern -> pattern.matches(PathContainer.parsePath(uri)) }
            }
        }
    }

    @Around("@annotation(nonAuditable)")
    fun handleNonAuditable(joinPoint: ProceedingJoinPoint, nonAuditable: NonAuditable): Any? {
        val className = joinPoint.target.javaClass.simpleName
        val methodName = joinPoint.signature.name

        // Cache the audit configuration
        val key = "$className.$methodName"
        val uriPatterns = extractUriPatterns(joinPoint)
        val pathPatterns = uriPatterns.mapNotNull { pattern ->
            try {
                pathPatternParser.parse(pattern)
            } catch (e: Exception) {
                null
            }
        }

        val auditConfig = AuditConfig(
            skipAudit = !nonAuditable.onlyResponse,
            ignoreResponse = nonAuditable.onlyResponse,
            uriPatterns = uriPatterns,
            pathPatterns = pathPatterns
        )
        auditConfigCache[key] = auditConfig

        // Proceed with the original method execution
        return joinPoint.proceed()
    }

    private fun extractUriPatterns(joinPoint: ProceedingJoinPoint): List<String> {
        val method = joinPoint.signature
        val methodAnnotations = method.declaringType.getMethod(
            method.name,
            *joinPoint.args.map { it?.javaClass ?: Any::class.java }.toTypedArray()
        ).annotations
        val classAnnotations = method.declaringType.annotations

        // Extract URI patterns from class-level @RequestMapping
        val classUriPatterns = mutableListOf<String>()
        classAnnotations.forEach { annotation ->
            if (annotation.annotationClass.simpleName == "RequestMapping") {
                val value = safeGetStringArray(annotation, "value")
                val path = safeGetStringArray(annotation, "path")
                classUriPatterns.addAll(value + path)
            }
        }

        // Extract URI patterns from method-level annotations
        val methodUriPatterns = mutableListOf<String>()
        methodAnnotations.forEach { annotation ->
            when (annotation.annotationClass.simpleName) {
                "RequestMapping" -> {
                    val value = safeGetStringArray(annotation, "value")
                    val path = safeGetStringArray(annotation, "path")
                    methodUriPatterns.addAll(value + path)
                }

                "GetMapping", "PostMapping", "PutMapping", "PatchMapping", "DeleteMapping" -> {
                    val value = safeGetStringArray(annotation, "value")
                    val path = safeGetStringArray(annotation, "path")
                    methodUriPatterns.addAll(value + path)
                }
            }
        }

        // Combine class and method patterns
        val combinedPatterns = mutableListOf<String>()

        if (classUriPatterns.isEmpty()) {
            // No class-level mapping, use method patterns as-is
            combinedPatterns.addAll(methodUriPatterns)
        } else {
            // Combine class and method patterns
            classUriPatterns.forEach { classPattern ->
                if (methodUriPatterns.isEmpty()) {
                    // No method-level mapping, use class pattern
                    combinedPatterns.add(classPattern)
                } else {
                    // Combine class + method patterns
                    methodUriPatterns.forEach { methodPattern ->
                        val combined = combineUriPatterns(classPattern, methodPattern)
                        combinedPatterns.add(combined)
                    }
                }
            }
        }

        return combinedPatterns.filter { it.isNotEmpty() }
    }

    private fun combineUriPatterns(classPattern: String, methodPattern: String): String {
        val cleanClassPattern = classPattern.removeSuffix("/")
        val cleanMethodPattern = if (methodPattern.startsWith("/")) methodPattern else "/$methodPattern"
        return cleanClassPattern + cleanMethodPattern
    }

    @Suppress("UNCHECKED_CAST")
    private fun safeGetStringArray(annotation: Annotation, methodName: String): Array<String> {
        return try {
            val result = annotation.javaClass.getMethod(methodName).invoke(annotation)
            when (result) {
                is Array<*> -> result.filterIsInstance<String>().toTypedArray()
                //is Array<String> -> result
                else -> emptyArray()
            }
        } catch (e: Exception) {
            emptyArray()
        }
    }
}

/**
 * Data class to hold audit configuration for a method
 */
data class AuditConfig(
    val skipAudit: Boolean = false,
    val ignoreResponse: Boolean = false,
    val uriPatterns: List<String> = emptyList(),
    val pathPatterns: List<PathPattern> = emptyList()
)