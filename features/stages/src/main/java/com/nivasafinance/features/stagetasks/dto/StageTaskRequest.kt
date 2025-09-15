package com.nivasafinance.features.stagetasks.dto

import java.util.*

data class StageTaskRequest(
    val stageId: UUID,
    val taskId: UUID,
    val extData: Map<String, Any>? = null
)