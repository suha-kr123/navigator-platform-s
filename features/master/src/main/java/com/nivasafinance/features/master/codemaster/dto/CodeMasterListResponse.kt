package com.nivasafinance.features.master.codemaster.dto

data class CodeMasterListResponse(
    val codeName: String,
    val values: List<CodeValueResponse>
)
