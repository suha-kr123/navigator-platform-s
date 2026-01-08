package com.nivasafinance.features.master.codemaster.controller;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.common.annotations.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/code")
@RequiredArgsConstructor
public class CodeMasterController {
    
    private final CodeMasterService codeMasterService;
    
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
}

