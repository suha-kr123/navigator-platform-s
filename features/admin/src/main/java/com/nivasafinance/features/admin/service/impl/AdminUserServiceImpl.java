package com.nivasafinance.features.admin.service.impl;

import com.nivasafinance.features.admin.exception.AdminExceptionFactory;
import com.nivasafinance.features.admin.service.AdminUserService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserWriteService userWriteService;
    private final UserReadService userReadService;
    private final MessageSource messageSource;

    @Override
    public void deleteUser(String username) {
        userWriteService.deleteUser(username);
    }

    @Override
    public void undoDeleteUser(String username) {
        UserResponse user = userReadService.adminGetUserByUsername(username);
        if (user.getPersonResponse() == null) {
            throw AdminExceptionFactory.cannotRestoreUserPersonDeleted(messageSource);
        }
        userWriteService.undoDeleteUser(username);
    }
}
