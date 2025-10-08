package com.nivasafinance.common.audit

/**
 * Annotation to mark API endpoints as non-auditable or partially auditable.
 *
 * @param onlyResponse If true, only the response body will be ignored from audit logging.
 *                    If false, the entire request will be skipped from audit logging.
 *                    Default is true.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NonAuditable(
    val onlyResponse: Boolean = true
)