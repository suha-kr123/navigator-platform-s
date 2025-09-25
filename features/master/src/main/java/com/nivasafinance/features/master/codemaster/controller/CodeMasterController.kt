package com.nivasafinance.features.master.codemaster.controller

import com.nivasafinance.features.master.codemaster.dto.CodeMasterListResponse
import com.nivasafinance.features.master.codemaster.service.CodeMasterService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/code-master")
class CodeMasterController(
    private val codeMasterService: CodeMasterService
) {

    @GetMapping("/{codeName}")
    fun getAllCodeMastersByCodeName(@PathVariable codeName: String): ResponseEntity<CodeMasterListResponse> {
        val response = codeMasterService.getAllCodeMastersByCodeName(codeName)
        return ResponseEntity.ok(response)
    }
}
