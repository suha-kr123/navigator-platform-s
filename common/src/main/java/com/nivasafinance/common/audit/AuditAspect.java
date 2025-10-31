package com.nivasafinance.common.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AOP Aspect to handle @NonAuditable annotation and cache audit configuration
 * for efficient lookup in the ApiAuditFilter.
 */
@Aspect
@Component
public class AuditAspect {

    // Cache to store audit configuration for each method
    // Key: "className.methodName" format
    // Value: AuditConfig containing the audit settings
    private static final ConcurrentHashMap<String, AuditConfig> auditConfigCache = new ConcurrentHashMap<>();

    // PathPattern parser for efficient URL matching
    private static final PathPatternParser pathPatternParser = new PathPatternParser();

    /**
     * Get audit configuration for a specific method
     */
    public static AuditConfig getAuditConfig(String className, String methodName) {
        String key = className + "." + methodName;
        return auditConfigCache.get(key);
    }

    /**
     * Get audit configuration for a specific method by URI pattern
     * This is used by the filter when we have URI but not exact method info
     */
    public static AuditConfig getAuditConfigByUri(String uri) {
        return auditConfigCache.values().stream()
                .filter(config -> config.getPathPatterns().stream()
                        .anyMatch(pattern -> pattern.matches(PathContainer.parsePath(uri))))
                .findFirst()
                .orElse(null);
    }

    @Around("@annotation(nonAuditable)")
    public Object handleNonAuditable(ProceedingJoinPoint joinPoint, NonAuditable nonAuditable) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // Cache the audit configuration
        String key = className + "." + methodName;
        List<String> uriPatterns = extractUriPatterns(joinPoint);
        List<PathPattern> pathPatterns = uriPatterns.stream()
                .map(pattern -> {
                    try {
                        return pathPatternParser.parse(pattern);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(pattern -> pattern != null)
                .collect(Collectors.toList());

        AuditConfig auditConfig = new AuditConfig(
                !nonAuditable.onlyResponse(),
                nonAuditable.onlyResponse(),
                uriPatterns,
                pathPatterns
        );
        auditConfigCache.put(key, auditConfig);

        // Proceed with the original method execution
        return joinPoint.proceed();
    }

    private List<String> extractUriPatterns(ProceedingJoinPoint joinPoint) {
        try {
            Class<?> declaringType = joinPoint.getSignature().getDeclaringType();
            String methodName = joinPoint.getSignature().getName();
            
            Class<?>[] parameterTypes = Arrays.stream(joinPoint.getArgs())
                    .map(arg -> arg != null ? arg.getClass() : Object.class)
                    .toArray(Class<?>[]::new);
                    
            Annotation[] methodAnnotations = declaringType.getMethod(methodName, parameterTypes).getAnnotations();
            Annotation[] classAnnotations = declaringType.getAnnotations();

            // Extract URI patterns from class-level @RequestMapping
            List<String> classUriPatterns = new ArrayList<>();
            for (Annotation annotation : classAnnotations) {
                if ("RequestMapping".equals(annotation.annotationType().getSimpleName())) {
                    String[] value = safeGetStringArray(annotation, "value");
                    String[] path = safeGetStringArray(annotation, "path");
                    classUriPatterns.addAll(Arrays.asList(value));
                    classUriPatterns.addAll(Arrays.asList(path));
                }
            }

            // Extract URI patterns from method-level annotations
            List<String> methodUriPatterns = new ArrayList<>();
            for (Annotation annotation : methodAnnotations) {
                String simpleName = annotation.annotationType().getSimpleName();
                if ("RequestMapping".equals(simpleName) || 
                    "GetMapping".equals(simpleName) || 
                    "PostMapping".equals(simpleName) || 
                    "PutMapping".equals(simpleName) || 
                    "PatchMapping".equals(simpleName) || 
                    "DeleteMapping".equals(simpleName)) {
                    String[] value = safeGetStringArray(annotation, "value");
                    String[] path = safeGetStringArray(annotation, "path");
                    methodUriPatterns.addAll(Arrays.asList(value));
                    methodUriPatterns.addAll(Arrays.asList(path));
                }
            }

            // Combine class and method patterns
            List<String> combinedPatterns = new ArrayList<>();

            if (classUriPatterns.isEmpty()) {
                // No class-level mapping, use method patterns as-is
                combinedPatterns.addAll(methodUriPatterns);
            } else {
                // Combine class and method patterns
                for (String classPattern : classUriPatterns) {
                    if (methodUriPatterns.isEmpty()) {
                        // No method-level mapping, use class pattern
                        combinedPatterns.add(classPattern);
                    } else {
                        // Combine class + method patterns
                        for (String methodPattern : methodUriPatterns) {
                            String combined = combineUriPatterns(classPattern, methodPattern);
                            combinedPatterns.add(combined);
                        }
                    }
                }
            }

            return combinedPatterns.stream()
                    .filter(pattern -> !pattern.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String combineUriPatterns(String classPattern, String methodPattern) {
        String cleanClassPattern = classPattern.endsWith("/") 
                ? classPattern.substring(0, classPattern.length() - 1) 
                : classPattern;
        String cleanMethodPattern = methodPattern.startsWith("/") 
                ? methodPattern 
                : "/" + methodPattern;
        return cleanClassPattern + cleanMethodPattern;
    }

    private String[] safeGetStringArray(Annotation annotation, String methodName) {
        try {
            Object result = annotation.annotationType().getMethod(methodName).invoke(annotation);
            if (result instanceof String[]) {
                return (String[]) result;
            } else if (result instanceof Object[]) {
                return Arrays.stream((Object[]) result)
                        .filter(obj -> obj instanceof String)
                        .map(obj -> (String) obj)
                        .toArray(String[]::new);
            }
            return new String[0];
        } catch (Exception e) {
            return new String[0];
        }
    }
}

