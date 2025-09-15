package com.nivasafinance.features.lead.dto

import java.util.*

data class CreateTaskRequest(
    val taskKey: String,
    val assignedTo: String? = null,
    val taskData: String? = null,
    val dueAt: String? = null
)
