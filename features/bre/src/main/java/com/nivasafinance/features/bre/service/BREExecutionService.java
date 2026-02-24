package com.nivasafinance.features.bre.service;

import com.nivasafinance.features.bre.dto.BREExecutionRequest;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;

import java.util.concurrent.CompletableFuture;

public interface BREExecutionService {
    CompletableFuture<BREExecutionResponse> execute(String uname, BREExecutionRequest request);
}
