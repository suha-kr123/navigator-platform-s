package com.nivasafinance.features.call.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.service.CallWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.V1 + "/call")
@RequiredArgsConstructor
public class CallLogController {

    private final CallWriteService callWriteService;

    @PutMapping("/provider/{providerId}")
    public ResponseEntity<Void> updateCallLogByProviderId(
            @PathVariable String providerId,
            @Valid @RequestBody UpdateCallLog request
    ) {
        callWriteService.updateCallLogByProviderId(providerId, request);
        return ResponseEntity.noContent().build();
    }
}


