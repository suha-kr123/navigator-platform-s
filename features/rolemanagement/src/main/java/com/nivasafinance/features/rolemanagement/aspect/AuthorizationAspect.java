package com.nivasafinance.features.rolemanagement.aspect;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.ForbiddenException;
import com.nivasafinance.common.exception.UnauthorizedException;
import com.nivasafinance.common.annotations.RequirePermission;
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
        
        String permissionName = requirePermission.permissionName();
        boolean hasPermission = permissionCheckerService.checkPermissionForUser(
                userName,
                permissionName
        );
        
        if (!hasPermission) {
            logger.warn(
                    "Access denied for user {} to permission {}",
                    userName,
                    permissionName
            );
            throw new ForbiddenException(
                    "Insufficient permissions: " + permissionName
            );
        }
        
        logger.debug(
                "Permission granted for user {} to permission {}",
                userName,
                permissionName
        );
        return joinPoint.proceed();
    }
}

