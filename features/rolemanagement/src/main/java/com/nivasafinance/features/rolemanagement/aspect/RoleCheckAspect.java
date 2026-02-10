package com.nivasafinance.features.rolemanagement.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Arrays;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.ForbiddenException;
import com.nivasafinance.common.exception.UnauthorizedException;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class RoleCheckAspect {

    private final UserRoleService userRoleService;

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        String username = UserContext.getUsername();
        if (username == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        List<String> roles = userRoleService.getRolesByUsername(username);
        if (roles.isEmpty() || !Arrays.asList(requireRole.value()).containsAll(roles)) {
            throw new ForbiddenException("User does not have the required role");
        }
        return joinPoint.proceed();
    }

}
