package com.nivasafinance.common.exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
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
    fun handleResourceNotFoundException(ex: ResourceNotFoundException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.NOT_FOUND,
            errorCode = "RESOURCE_NOT_FOUND",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.NOT_FOUND)
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

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "BAD_REQUEST",
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

    @ExceptionHandler(ResourceConflictException::class)
    fun handleResourceConflictException(ex: ResourceConflictException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.CONFLICT,
            errorCode = "RESOURCE_CONFLICT",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.CONFLICT)
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

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(ex: ForbiddenException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.FORBIDDEN,
            errorCode = "FORBIDDEN",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Validation failed.") }
        val apiError = ApiError(
            fieldErrors = errors,
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
        val apiError = ApiError(
            error = "Invalid JSON format: ${ex.message}",
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "INVALID_JSON",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InvalidFormatException::class)
    fun handleInvalidFormatException(ex: InvalidFormatException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = "Invalid format: ${ex.message}",
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "INVALID_FORMAT",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
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
