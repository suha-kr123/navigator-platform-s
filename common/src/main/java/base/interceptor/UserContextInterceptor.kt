package base.interceptor

import base.context.UserContext
import base.model.UserInfo
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

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
            val username = decodedJWT.getClaim("username")?.asString() ?: throw RuntimeException("Invalid token: username is missing")
            val email = decodedJWT.getClaim("email")?.asString() ?: "unknown"
            val phoneNumber = decodedJWT.getClaim("phone_number")?.asString() ?: "unknown"
            val roles = decodedJWT.getClaim("cognito:groups")
                ?.asList(String::class.java)
                .orEmpty()

            val userInfo = UserInfo(username, email, phoneNumber, roles)
            UserContext.setUserInfo(userInfo)
            logger.info("UserContext set with: $userInfo")

            true
        } catch (e: JWTDecodeException) {
            logger.warn("Invalid JWT token", e)
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Unauthorized: Invalid token")
            false
        }
    }

    // Optionally handle logic after the controller has executed, before view rendering
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: org.springframework.web.servlet.ModelAndView?
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
