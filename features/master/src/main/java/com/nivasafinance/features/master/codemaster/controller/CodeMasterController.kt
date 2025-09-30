package com.nivasafinance.features.master.codemaster.controller

import com.nivasafinance.features.master.codemaster.dto.CodeMasterListResponse
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse
import com.nivasafinance.features.master.codemaster.service.CodeMasterService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/code")
class CodeMasterController(
    private val codeMasterService: CodeMasterService
) {

    @GetMapping("/{codeKey}")
    fun getAllCodeValuesByCodeKey(
        @PathVariable codeKey: String,
        @RequestParam(defaultValue = "true") onlyActive: Boolean
    ): ResponseEntity<CodeMasterListResponse> {
        val response = codeMasterService.getAllCodeValuesByCodeKey(codeKey, onlyActive)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{codeKey}/childs")
    fun getMasterCodeChildrenWithValues(
        @PathVariable codeKey: String,
        @RequestParam(defaultValue = "true") onlyActive: Boolean
    ): ResponseEntity<List<MasterCodeWithValuesResponse>> {
        val response = codeMasterService.getMasterCodeChildrenWithValues(codeKey, onlyActive)
        return ResponseEntity.ok(response)
    }
}
