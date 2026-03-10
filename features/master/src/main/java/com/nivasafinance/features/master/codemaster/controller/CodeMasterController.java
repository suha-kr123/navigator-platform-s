package com.nivasafinance.features.master.codemaster.controller;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeTreeResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeSearchMultiSectionResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeSearchRequest;
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
import com.nivasafinance.features.master.codemaster.dto.*;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/code")
@RequiredArgsConstructor
public class CodeMasterController {

    private final CodeMasterService codeMasterService;
    private final CodeValueMasterService codeValueMasterService;

    @PostMapping("/search")
    @RequireRole(value = { "ADMIN" })
    public ResponseEntity<MasterCodeSearchMultiSectionResponse> searchMasterCodes(
            @Valid @RequestBody MasterCodeSearchRequest searchRequest,
            @Valid PaginationRequest paginationRequest) {
        MasterCodeSearchMultiSectionResponse response = codeMasterService.searchMasterCodes(
                searchRequest.getSearchTerm(), searchRequest.getSearchContexts(), searchRequest.getCodeKey(),
                paginationRequest);
        return ResponseEntity.ok(response);
    }

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
            @RequestParam(defaultValue = "true") Boolean onlyActive,
            @RequestParam(required = false) String context) {
        try {
            List<CodeValueResponse> response = codeMasterService.getAllCodeValuesByCodeKey(codeKey, onlyActive, context);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{codeKey}/childs")
    @RequirePermission(permissionName = "READ_MASTER_CODE")
    public ResponseEntity<List<MasterCodeWithValuesResponse>> getMasterCodeChildrenWithValues(
            @PathVariable String codeKey,
            @RequestParam(defaultValue = "true") Boolean onlyActive,
            @RequestParam(required = false) String context) {
        try {
            List<MasterCodeWithValuesResponse> response = codeMasterService.getMasterCodeChildrenWithValues(codeKey, onlyActive, context);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{codeKey}/values/paginated")
    @RequirePermission(permissionName = "READ_MASTER_CODE")
    public ResponseEntity<PaginatedResponse<CodeValueResponse>> getCodeValuesByCodeKeyPaginated(
            @PathVariable String codeKey,
            @RequestParam(defaultValue = "true") Boolean onlyActive,
            @RequestParam(required = false) String context,
            @Valid PaginationRequest paginationRequest) {
        try {
            return ResponseEntity.ok(codeMasterService.getCodeValuesByCodeKeyPaginated(codeKey, onlyActive, context, paginationRequest));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{codeKey}/childs/paginated")
    @RequirePermission(permissionName = "READ_MASTER_CODE")
    public ResponseEntity<PaginatedResponse<MasterCodeWithValuesResponse>> getMasterCodeChildrenWithValuesPaginated(
            @PathVariable String codeKey,
            @RequestParam(defaultValue = "true") Boolean onlyActive,
            @RequestParam(required = false) String context,
            @Valid PaginationRequest paginationRequest) {
        try {
            return ResponseEntity.ok(codeMasterService.getMasterCodeChildrenWithValuesPaginated(codeKey, onlyActive, context, paginationRequest));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
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

    @GetMapping("/tree/{parentCodeKey}")
    @RequireRole(value = { "ADMIN" })
    public ResponseEntity<List<MasterCodeTreeResponse>> getMasterCodeTree(
            @PathVariable String parentCodeKey) {
        List<MasterCodeTreeResponse> response = codeMasterService.getMasterCodeTree(parentCodeKey);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/tree/{parentCodeKey}/child")
    @RequireRole(value = { "ADMIN" })
    public ResponseEntity<List<MasterCodeTreeResponse>> addChildToTree(
            @PathVariable String parentCodeKey,
            @RequestBody MasterCodeWithValuesRequest child) {
        List<MasterCodeTreeResponse> response = codeMasterService.addChildToTree(parentCodeKey, child);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/values/icons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    @RequireRole(value = { "ADMIN" })
    public ResponseEntity<CodeValueResponse> uploadIcon(
            @Valid @RequestPart("metadata") MasterCodeValueIconUploadRequest request,
            @RequestPart("file") MultipartFile file) {
        CodeValueResponse response = codeValueMasterService.uploadIcon(request, file);
        return ResponseEntity.ok(response);
    }
}
