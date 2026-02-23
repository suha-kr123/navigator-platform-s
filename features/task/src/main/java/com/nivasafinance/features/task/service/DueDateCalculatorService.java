package com.nivasafinance.features.task.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface DueDateCalculatorService {
    
    LocalDateTime calculateDueDate(String expression, Map<String, Object> context);
}

