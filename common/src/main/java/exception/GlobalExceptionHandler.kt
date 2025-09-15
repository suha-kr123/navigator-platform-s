package exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.nivasafinance.features.tasks.exception.*
import com.nivasafinance.features.stages.exception.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.util.UUID

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        exception: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = exception.localizedMessage,
            statusCode = HttpStatus.NOT_FOUND,
            errorCode = "RESOURCE_NOT_FOUND",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(
        ex: BadRequestException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "BAD_REQUEST",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "VALIDATION_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val fieldErrors = ex.bindingResult.fieldErrors
        val errorMessages = fieldErrors.joinToString(", ") { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }

        val apiError = ApiError(
            error = "Validation failed: $errorMessages",
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "VALIDATION_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val errorMessage = when (val cause = ex.cause) {
            is InvalidFormatException -> {
                val fieldName = cause.pathReference.toString().split("->").lastOrNull()?.trim() ?: "unknown"
                val invalidValue = cause.value?.toString() ?: "null"

                when {
                    fieldName.contains("stage", ignoreCase = true) -> {
                        val validValues = listOf(
                            "INQUIRY",
                            "DOCUMENTATION",
                            "PROCESSING",
                            "SANCTION",
                            "DISBURSEMENT",
                            "CLOSED"
                        )
                        "Invalid value '$invalidValue' for field '$fieldName'. " +
                            "Valid values are: ${validValues.joinToString(", ")}"
                    }
                    fieldName.contains("status", ignoreCase = true) -> {
                        val validValues = listOf("ACTIVE", "ON_HOLD", "REJECTED", "CANCELLED", "COMPLETED")
                        "Invalid value '$invalidValue' for field '$fieldName'. " +
                            "Valid values are: ${validValues.joinToString(", ")}"
                    }
                    else -> "Invalid value '$invalidValue' for field '$fieldName'"
                }
            }
            else -> "Invalid request body: ${ex.message}"
        }

        val apiError = ApiError(
            error = errorMessage,
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "INVALID_REQUEST_BODY",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(ex: ConflictException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.CONFLICT,
            errorCode = "CONFLICT",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFoundException(ex: TaskNotFoundException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.NOT_FOUND,
            errorCode = "TASK_NOT_FOUND",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(TaskValidationException::class)
    fun handleTaskValidationException(ex: TaskValidationException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "TASK_VALIDATION_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(TaskConflictException::class)
    fun handleTaskConflictException(ex: TaskConflictException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.CONFLICT,
            errorCode = "TASK_CONFLICT",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(TaskOperationException::class)
    fun handleTaskOperationException(ex: TaskOperationException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
            errorCode = "TASK_OPERATION_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(StageNotFoundException::class)
    fun handleStageNotFoundException(ex: StageNotFoundException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.NOT_FOUND,
            errorCode = "STAGE_NOT_FOUND",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(StageValidationException::class)
    fun handleStageValidationException(ex: StageValidationException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "STAGE_VALIDATION_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(StageConflictException::class)
    fun handleStageConflictException(ex: StageConflictException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.CONFLICT,
            errorCode = "STAGE_CONFLICT",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(StageOperationException::class)
    fun handleStageOperationException(ex: StageOperationException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
            errorCode = "STAGE_OPERATION_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.UNAUTHORIZED,
            errorCode = "UNAUTHORIZED",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
            errorCode = "INTERNAL_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun generateRequestId(): String = UUID.randomUUID().toString()
}
