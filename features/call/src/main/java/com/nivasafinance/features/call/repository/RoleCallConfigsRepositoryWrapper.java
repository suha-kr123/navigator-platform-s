package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.RoleCallConfigs;
import com.nivasafinance.features.call.exception.RoleConfigsExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleCallConfigsRepositoryWrapper {

    private final RoleCallConfigsRepository roleCallConfigsRepository;
    private final MessageSource messageSource;

    public RoleCallConfigs findByRoleWithException(String role) {
        try {
            return roleCallConfigsRepository.findByRole(role)
                    .orElseThrow(() -> RoleConfigsExceptionFactory.notFound(role, messageSource));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve role call configuration " + role , e);
        }
    }
}

