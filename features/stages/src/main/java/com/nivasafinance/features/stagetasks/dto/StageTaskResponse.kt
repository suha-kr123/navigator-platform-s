package com.nivasafinance.features.stagetasks.dto

import java.util.*

data class StageTaskResponse(
    val id: UUID,
    val stageId: UUID,
    val taskId: UUID,
    val extData: Map<String, Any>? = null
)