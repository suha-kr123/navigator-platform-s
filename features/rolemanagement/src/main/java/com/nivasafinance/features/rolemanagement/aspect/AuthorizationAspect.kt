package com.nivasafinance.features.rolemanagement.aspect

import com.nivasafinance.common.exception.ForbiddenException
import com.nivasafinance.common.exception.UnauthorizedException
import com.nivasafinance.features.rolemanagement.annotation.RequirePermission
import com.nivasafinance.features.rolemanagement.permissionchecker.PermissionCheckerService
import com.nivasafinance.security.context.UserContext
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Aspect
@Component
@Order(1)
class AuthorizationAspect(
    private val permissionCheckerService: PermissionCheckerService
) {

    private val logger = LoggerFactory.getLogger(AuthorizationAspect::class.java)

    @Around("@annotation(requirePermission)")
    fun checkPermission(joinPoint: ProceedingJoinPoint, requirePermission: RequirePermission): Any? {
        val userInfo = UserContext.getUserInfo()
            ?: throw UnauthorizedException("User not authenticated")

        // Check permission using the existing permission checker service
        val hasPermission = permissionCheckerService.checkPermissionForUser(
            userInfo.username,
            requirePermission.action,
            requirePermission.module,
            requirePermission.operation
        )

        if (!hasPermission) {
            logger.warn(
                "Access denied for user ${userInfo.username} to ${requirePermission.action} ${requirePermission.module}"
            )
            throw ForbiddenException(
                "Insufficient permissions: ${requirePermission.action} on ${requirePermission.module}"
            )
        }

        logger.debug(
            "Permission granted for user ${userInfo.username} to ${requirePermission.action} ${requirePermission.module}"
        )
        return joinPoint.proceed()
    }
}
