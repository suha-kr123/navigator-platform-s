package com.nivasafinance.features.rolemanagement.aspect;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.ForbiddenException;
import com.nivasafinance.common.exception.UnauthorizedException;
import com.nivasafinance.features.rolemanagement.annotation.RequirePermission;
import com.nivasafinance.features.rolemanagement.permissionchecker.PermissionCheckerService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class AuthorizationAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAspect.class);
    
    private final PermissionCheckerService permissionCheckerService;
    
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String userName = UserContext.getUsername();
        if (userName == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        boolean hasPermission = permissionCheckerService.checkPermissionForUser(
                userName,
                requirePermission.action(),
                requirePermission.module(),
                requirePermission.operation()
        );
        
        if (!hasPermission) {
            logger.warn(
                    "Access denied for user {} to {} {}",
                    userName,
                    requirePermission.action(),
                    requirePermission.module()
            );
            throw new ForbiddenException(
                    "Insufficient permissions: " + requirePermission.action() + " on " + requirePermission.module()
            );
        }
        
        logger.debug(
                "Permission granted for user {} to {} {}",
                userName,
                requirePermission.action(),
                requirePermission.module()
        );
        return joinPoint.proceed();
    }
}

