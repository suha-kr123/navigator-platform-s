package com.nivasafinance.security.model

data class UserInfo(
    val userId: String,
    val username: String,
    val email: String?,
    val phoneNumber: String?
    // val roles: List<String> = emptyList() // Removed for now
)
