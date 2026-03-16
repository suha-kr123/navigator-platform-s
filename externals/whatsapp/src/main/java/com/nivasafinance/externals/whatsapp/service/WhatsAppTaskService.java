package com.nivasafinance.externals.whatsapp.service;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppTaskRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppTaskResponse;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppUpdateTaskRequest;

public interface WhatsAppTaskService {
    WhatsAppTaskResponse createTask(WhatsAppTaskRequest request);
    
    WhatsAppTaskResponse updateTask(WhatsAppUpdateTaskRequest request);
}
