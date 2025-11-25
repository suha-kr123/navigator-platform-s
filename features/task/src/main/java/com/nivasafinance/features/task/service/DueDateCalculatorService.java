package com.nivasafinance.features.task.service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for calculating task due dates using SpEL expressions.
 */
public interface DueDateCalculatorService {
    
    /**
     * Calculates the due date based on a SpEL expression.
     * 
     * @param expression The SpEL expression to evaluate (e.g., "T(java.time.LocalDateTime).now().plusDays(3)")
     * @param context Variables available in the expression context (e.g., entityInfo, stageKey, assignedTo)
     * @return The calculated due date, or null if expression is null/empty or evaluation fails
     */
    LocalDateTime calculateDueDate(String expression, Map<String, Object> context);
}

