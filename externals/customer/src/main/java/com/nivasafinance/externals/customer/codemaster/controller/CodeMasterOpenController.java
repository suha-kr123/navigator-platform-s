package com.nivasafinance.externals.customer.codemaster.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/codes")
@RequiredArgsConstructor
public class CodeMasterOpenController {

    private final CodeMasterService codeMasterService;

    @GetMapping("/{codeKey}")
    public ResponseEntity<List<CodeValueResponse>> getCodeValuesByCodeKey(
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
}
