package com.nivasafinance.features.stagetasks.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object StageTaskExceptionFactory {

    fun stageTaskNotFound(id: String): ResponseStatusException {
        return ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "StageTask with id '$id' not found"
        )
    }

    fun stageTaskNotFound(id: java.util.UUID): ResponseStatusException {
        return stageTaskNotFound(id.toString())
    }

    fun stageTaskConflict(message: String): ResponseStatusException {
        return ResponseStatusException(
            HttpStatus.CONFLICT,
            message
        )
    }

    fun stageTaskValidationFailed(message: String): ResponseStatusException {
        return ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "StageTask validation failed: $message"
        )
    }

    fun stageTaskOperationFailed(message: String): ResponseStatusException {
        return ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "StageTask operation failed: $message"
        )
    }

    fun duplicateStageTaskMapping(stageId: String, taskId: String): ResponseStatusException {
        return stageTaskConflict(
            "StageTask mapping already exists for stage '$stageId' and task '$taskId'"
        )
    }

    fun duplicateStageTaskMapping(stageId: java.util.UUID, taskId: java.util.UUID): ResponseStatusException {
        return duplicateStageTaskMapping(stageId.toString(), taskId.toString())
    }
}
