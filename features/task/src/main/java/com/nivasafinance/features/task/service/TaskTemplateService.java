package com.nivasafinance.features.task.service;

import com.nivasafinance.features.task.dto.TaskTemplateResponse;

public interface TaskTemplateService {
    TaskTemplateResponse getTaskTemplate(String taskConfigKey, String officeKey);
}

