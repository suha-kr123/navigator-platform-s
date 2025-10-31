package com.nivasafinance.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark API endpoints as non-auditable or partially auditable.
 *
 * @param onlyResponse If true, only the response body will be ignored from audit logging.
 *                    If false, the entire request will be skipped from audit logging.
 *                    Default is true.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NonAuditable {
    boolean onlyResponse() default true;
}

