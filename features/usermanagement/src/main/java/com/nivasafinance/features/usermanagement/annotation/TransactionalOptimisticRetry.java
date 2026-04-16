package com.nivasafinance.features.usermanagement.annotation;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Transactional
@Retryable(
    retryFor = { OptimisticLockingFailureException.class },
    maxAttempts = 3,
    backoff = @Backoff(
        delay = 300
    )
)
public @interface TransactionalOptimisticRetry {
}
