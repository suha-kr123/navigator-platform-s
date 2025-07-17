package exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(exception: ResourceNotFoundException): ResponseEntity<ApiError> {
        val apiError = ApiError(error = exception.localizedMessage, statusCode = HttpStatus.NOT_FOUND)
        return ResponseEntity(apiError, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException): ResponseEntity<ApiError> {
        val apiError = ApiError(error = ex.localizedMessage, statusCode = HttpStatus.BAD_REQUEST)
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ApiError> {
        val apiError = ApiError(error = ex.localizedMessage, statusCode = HttpStatus.INTERNAL_SERVER_ERROR)
        return ResponseEntity(apiError, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
