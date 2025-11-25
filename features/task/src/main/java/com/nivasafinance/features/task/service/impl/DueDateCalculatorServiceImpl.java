package com.nivasafinance.features.task.service.impl;

import com.nivasafinance.features.task.service.DueDateCalculatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service implementation for calculating task due dates using Spring Expression Language (SpEL).
 * 
 * SpEL expressions can access context variables like:
 * - now: Current LocalDateTime
 * - entityInfo: Entity information (Lead, etc.)
 * - stageKey: Current stage key
 * - assignedTo: Assigned user/role
 * 
 * Examples:
 * - T(java.time.LocalDateTime).now().plusDays(3) - 3 days from now
 * - #now.plusDays(5) - 5 days from now
 * - #now.plusMinutes(30) - 30 minutes from now
 * - #now.plusHours(2) - 2 hours from now
 * - #now.plusWeeks(1) - 1 week from now
 * - #now.withHour(17).withMinute(0).withSecond(0) - end of business day today
 * - #now.plusDays((#entityInfo != null && #entityInfo.requestedAmount != null && #entityInfo.requestedAmount > 100000) ? 7 : 3) - conditional logic
 */
@Slf4j
@Service
public class DueDateCalculatorServiceImpl implements DueDateCalculatorService {

    private static final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public LocalDateTime calculateDueDate(String expression, Map<String, Object> context) {
        if (!StringUtils.hasText(expression)) {
            log.debug("Due date expression is null or empty, returning null");
            return null;
        }

        try {
            Expression spelExpression = parser.parseExpression(expression);
            EvaluationContext evaluationContext = createEvaluationContext(context);
            
            Object result = spelExpression.getValue(evaluationContext);
            
            if (result == null) {
                log.warn("Due date expression evaluated to null: {}", expression);
                return null;
            }
            
            if (result instanceof LocalDateTime) {
                return (LocalDateTime) result;
            }
            
            if (result instanceof java.util.Date) {
                return ((java.util.Date) result).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            }
            
            if (result instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) result).toLocalDateTime();
            }
            
            log.warn("Due date expression returned unsupported type: {} for expression: {}", 
                    result.getClass().getName(), expression);
            return null;
            
        } catch (EvaluationException e) {
            log.error("Failed to evaluate due date expression: {}", expression, e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error evaluating due date expression: {}", expression, e);
            return null;
        }
    }

    private EvaluationContext createEvaluationContext(Map<String, Object> context) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        
        // Add 'now' as a convenience variable (accessed with #now)
        evaluationContext.setVariable("now", LocalDateTime.now());
        
        // Add all context variables (accessed with #variableName)
        if (context != null) {
            context.forEach(evaluationContext::setVariable);
        }
        
        return evaluationContext;
    }
}

