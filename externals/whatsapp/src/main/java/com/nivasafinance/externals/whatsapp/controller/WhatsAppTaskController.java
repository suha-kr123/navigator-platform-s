package com.nivasafinance.externals.whatsapp.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppTaskRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppTaskResponse;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppUpdateTaskRequest;
import com.nivasafinance.externals.whatsapp.service.WhatsAppTaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.EXTERNAL_V1 + "/whatsapp")
@AllArgsConstructor
public class WhatsAppTaskController {

    private final WhatsAppTaskService whatsAppTaskService;

    @PostMapping("/tasks")
    public ResponseEntity<WhatsAppTaskResponse> createTask(
            @Valid @RequestBody WhatsAppTaskRequest request) {
        WhatsAppTaskResponse response = whatsAppTaskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/tasks/update")
    public ResponseEntity<WhatsAppTaskResponse> updateTask(
            @Valid @RequestBody WhatsAppUpdateTaskRequest request) {
        WhatsAppTaskResponse response = whatsAppTaskService.updateTask(request);
        return ResponseEntity.ok(response);
    }
}
