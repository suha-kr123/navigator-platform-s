package com.nivasafinance.features.rolemanagement.permission.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionBatchCheckResponse;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionCheckResponse;
import com.nivasafinance.features.rolemanagement.permission.service.PermissionCheckService;
import com.nivasafinance.features.rolemanagement.permissionchecker.PermissionCheckerService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionCheckServiceImpl implements PermissionCheckService {

	private final PermissionCheckerService permissionCheckerService;
	private final MessageSource messageSource;

	@Override
	public PermissionCheckResponse checkPermission(String permissionName) {

		String username = getCurrentUsername();

		boolean hasPermission = permissionCheckerService.checkPermissionForUser(username, permissionName);

		return PermissionCheckResponse.of(permissionName, hasPermission, username);
	}

	@Override
	public PermissionBatchCheckResponse checkPermissionsBatch(List<String> permissionNames) {

		String username = getCurrentUsername();

		Map<String, Boolean> permissions = checkPermissions(permissionNames, username);

		return PermissionBatchCheckResponse.of(permissions, username);
	}

	private String getCurrentUsername() {
		String username = UserContext.getUsername();
		if (!ValidationUtils.isNonNull(username)) {
			throw RoleManagementExceptionFactory.unauthorized(messageSource);
		}
		return username;
	}

	private Map<String, Boolean> checkPermissions(List<String> permissionNames, String username) {
		if (ValidationUtils.isNullOrEmpty(permissionNames)) {
			return Map.of();
		}

		return permissionNames.stream()
				.collect(Collectors.toMap(
						permission -> permission,
						permission -> permissionCheckerService.checkPermissionForUser(
								username,
								permission)));
	}
}
