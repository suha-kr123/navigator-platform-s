package com.nivasafinance.common.filter

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nivasafinance.common.audit.ApiAuditLog
import com.nivasafinance.common.audit.AuditAspect
import com.nivasafinance.common.audit.AuditConfig
import com.nivasafinance.common.service.ApiAuditService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.IOException
import java.time.LocalDateTime

@Component
class ApiAuditFilter(
    private val apiAuditService: ApiAuditService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Check if this request should be completely skipped from audit
        val auditConfig = getAuditConfigForRequest(request)
        if (auditConfig?.skipAudit == true) {
            filterChain.doFilter(request, response)
            return
        }

        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)
        val startTime = System.currentTimeMillis()

        var errorMessage: String? = null
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } catch (ex: IOException) {
            errorMessage = ex.message ?: "IO error"
            logger.error("IOException in API filter: ${ex.message}", ex)
            throw ex
        } catch (ex: ServletException) {
            errorMessage = ex.message ?: "Servlet error"
            logger.error("ServletException in API filter: ${ex.message}", ex)
            throw ex
        } finally {
            logAudit(wrappedRequest, wrappedResponse, startTime, errorMessage, auditConfig)
            wrappedResponse.copyBodyToResponse()
        }
    }

    private fun logAudit(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        startTime: Long,
        errorMessage: String?,
        auditConfig: AuditConfig? = null
    ) {
        val duration = System.currentTimeMillis() - startTime
        val requestBody = String(request.contentAsByteArray)
        val responseBody = String(response.contentAsByteArray)
        val username = extractUsernameFromJwt(request) ?: "system"

        val shouldIgnoreResponse = auditConfig?.ignoreResponse == true

        val auditLog = ApiAuditLog(
            username = username,
            method = request.method,
            uri = request.requestURI,
            ipAddress = request.remoteAddr,
            userAgent = request.getHeader("User-Agent"),

            requestBody = requestBody,
            responseBody = if (shouldIgnoreResponse) null else responseBody,
            responseStatus = response.status,
            errorMessage = errorMessage ?: extractErrorMessageFromResponse(responseBody),
            durationMs = duration,
            timestamp = LocalDateTime.now()
        )

        apiAuditService.saveAuditLog(auditLog)
    }

    private fun getAuditConfigForRequest(request: HttpServletRequest): AuditConfig? {
        // First try to get by URI pattern
        return AuditAspect.getAuditConfigByUri(request.requestURI)
    }

    fun extractUsernameFromJwt(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                val token = authHeader.removePrefix("Bearer ")
                val jwt = JWT.decode(token)
                return jwt.getClaim("username")?.asString()
            } catch (ex: JWTDecodeException) {
                logger.warn("Invalid JWT token: ${ex.message}", ex)
            }
        }

        return null
    }

    private fun extractErrorMessageFromResponse(responseBody: String): String? {
        return try {
            val json = jacksonObjectMapper().readTree(responseBody)
            json["error"]?.asText()
        } catch (ex: JsonProcessingException) {
            logger.debug("Invalid JSON: ${ex.message}", ex)
            null
        } catch (ex: IOException) {
            logger.debug("I/O error reading JSON: ${ex.message}", ex)
            null
        }
    }
}
