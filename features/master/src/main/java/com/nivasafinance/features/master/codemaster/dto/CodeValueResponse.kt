package com.nivasafinance.features.master.codemaster.dto

data class CodeValueResponse(
    val id: Int,
    val key: String,
    val value: Map<String, String>
)
