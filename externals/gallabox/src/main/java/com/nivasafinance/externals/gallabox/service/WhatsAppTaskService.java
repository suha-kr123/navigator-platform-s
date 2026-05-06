package com.nivasafinance.externals.gallabox.service;

import com.nivasafinance.externals.gallabox.dto.WhatsAppTaskRequest;
import com.nivasafinance.externals.gallabox.dto.WhatsAppTaskResponse;
import com.nivasafinance.externals.gallabox.dto.WhatsAppUpdateTaskRequest;

public interface WhatsAppTaskService {
    WhatsAppTaskResponse createTask(WhatsAppTaskRequest request);
    
    WhatsAppTaskResponse updateTask(WhatsAppUpdateTaskRequest request);
}
