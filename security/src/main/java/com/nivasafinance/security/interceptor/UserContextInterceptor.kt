package com.nivasafinance.security.interceptor


import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.nivasafinance.common.base.context.UserContext
import com.nivasafinance.common.base.model.UserInfo
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

@Component
class UserContextInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(UserContextInterceptor::class.java)

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    // Intercept the request before it reaches the controller
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith(BEARER_PREFIX)) {
            logger.warn("Missing or invalid Authorization header")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Unauthorized: Missing or invalid token")
            return false
        }

        val token = authHeader.removePrefix(BEARER_PREFIX)

        return try {
            val decodedJWT = JWT.decode(token)
            val userId = decodedJWT.getClaim("sub")?.asString()
                ?: throw IllegalArgumentException("Invalid token: userId is missing")
            val username = extractUsernameFromMetadata(decodedJWT)
            if (username == "unknown") {
                throw IllegalArgumentException("Invalid token: username is missing")
            }
            val email = decodedJWT.getClaim("email")?.asString()
            val phoneNumber = decodedJWT.getClaim("phone_number")?.asString()
            // val roles = decodedJWT.getClaim("cognito:groups")
            //     ?.asList(String::class.java)
            //     .orEmpty()

            val userInfo = UserInfo(userId, username, email, phoneNumber)
            UserContext.setUserInfo(userInfo)
            logger.info("UserContext set with: $userInfo")

            true
        } catch (e: JWTDecodeException) {
            logger.warn("Invalid JWT token", e)
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Unauthorized: Invalid token")
            false
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid token claims: ${e.message}", e)
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Unauthorized: Invalid token claims")
            false
        } catch (e: Exception) {
            logger.error("Unexpected error during token validation", e)
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write("Internal server error")
            false
        }
    }

    private fun extractUsernameFromMetadata(decodedJWT: com.auth0.jwt.interfaces.DecodedJWT): String {
        return try {
            // Get user_metadata claim
            val userMetadataClaim = decodedJWT.getClaim("user_metadata")
            
            if (userMetadataClaim.isNull) {
                logger.warn("user_metadata claim is null")
                return "unknown"
            }
            
            // Try to get username from user_metadata
            val userMetadata = userMetadataClaim.asMap()
            val username = userMetadata?.get("username")?.toString()
            
            if (username.isNullOrBlank()) {
                logger.warn("username not found in user_metadata")
                return "unknown"
            }
            
            username
        } catch (ex: Exception) {
            logger.warn("Error extracting username from metadata: ${ex.message}")
            "unknown"
        }
    }

    // Optionally handle logic after the controller has executed, before view rendering
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        // No logic needed for postHandle, method exists for completeness.
    }

    // Always executed after the request is fully completed
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        // Clear ThreadLocal to avoid memory leaks
        UserContext.clear()
        logger.info("UserContext cleared after request completion.")
    }
}