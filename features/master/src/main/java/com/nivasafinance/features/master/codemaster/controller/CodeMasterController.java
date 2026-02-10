package com.nivasafinance.features.master.codemaster.controller;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;

import jakarta.validation.Valid;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/code")
@RequiredArgsConstructor
public class CodeMasterController {
    
    private final CodeMasterService codeMasterService;
    private final CodeValueMasterService codeValueMasterService;

    @GetMapping("/master-codes/all")
    @RequireRole(value = { "ADMIN" })
    public ResponseEntity<PaginatedResponse<MasterCodeResponse>> getAllMasterCodes(
            @Valid PaginationRequest paginationRequest) {
        PaginatedResponse<MasterCodeResponse> response = codeMasterService.getAllMasterCodes(paginationRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{codeKey}")
    @RequirePermission(permissionName = "READ_MASTER_CODE")
    public ResponseEntity<List<CodeValueResponse>> getAllCodeValuesByCodeKey(
            @PathVariable String codeKey,
            @RequestParam(defaultValue = "true") Boolean onlyActive) {
        List<CodeValueResponse> response = codeMasterService.getAllCodeValuesByCodeKey(codeKey, onlyActive);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{codeKey}/childs")
    @RequirePermission(permissionName = "READ_MASTER_CODE")
    public ResponseEntity<List<MasterCodeWithValuesResponse>> getMasterCodeChildrenWithValues(
            @PathVariable String codeKey,
            @RequestParam(defaultValue = "true") Boolean onlyActive) {
        List<MasterCodeWithValuesResponse> response = codeMasterService.getMasterCodeChildrenWithValues(codeKey, onlyActive);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{masterCodeKey}/values")
    @RequireRole(value = { "ADMIN" })
    public ResponseEntity<MasterCodeValueResponse> createMasterCodeValues(
            @PathVariable String masterCodeKey,
            @RequestBody List<MasterCodeValueRequest> masterCodeValueRequests) {
        MasterCodeValueResponse response = codeValueMasterService.createMasterCodeValues(masterCodeKey, masterCodeValueRequests);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{masterCodeKey}/values/{masterCodeValueKey}/enable-disable")
    @RequireRole(value = { "ADMIN" })
    public ResponseEntity<MasterCodeValueResponse> enableDisableMasterCodeValue(
            @PathVariable String masterCodeKey,
            @PathVariable String masterCodeValueKey) {
        MasterCodeValueResponse response = codeValueMasterService.enableDisableMasterCodeValue(masterCodeKey,
                masterCodeValueKey);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{masterCodeKey}")
    @RequireRole(value = { "ADMIN" })
    public ResponseEntity<MasterCodeValueResponse> updateMasterCode(
            @PathVariable String masterCodeKey,
            @RequestBody MasterCodeWithValuesRequest masterCodeWithValuesRequest) {
        MasterCodeValueResponse response = codeMasterService.updateMasterCodeWithValues(masterCodeKey,
                masterCodeWithValuesRequest);
        return ResponseEntity.ok(response);
    }

}

