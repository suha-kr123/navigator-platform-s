package com.nivasafinance.features.bre.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.bre.dto.BREExecutionRequest;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;
import com.nivasafinance.features.bre.service.BREExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(ApiConstants.V1 + "/bre")
@RequiredArgsConstructor
public class BREExecutionController {

    private final BREExecutionService breExecutionService;

    @PostMapping("/{uname}/execute")
    @RequirePermission(permissionName = "EXECUTE_BRE") //sample API to test
    public CompletableFuture<ResponseEntity<BREExecutionResponse>> execute(
            @PathVariable String uname,
            @RequestBody(required = false) BREExecutionRequest request) {
        return breExecutionService.execute(uname, request != null ? request : new BREExecutionRequest())
                .thenApply(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }
}
