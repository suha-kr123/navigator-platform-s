package com.nivasafinance.features.usermanagement.validator;

import com.nivasafinance.common.exception.UnauthorizedException;
import com.nivasafinance.common.validator.UserStatusValidator;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.features.usermanagement.repository.UserRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStatusValidatorImpl implements UserStatusValidator {

    private static final Logger logger = LoggerFactory.getLogger(UserStatusValidatorImpl.class);

    private final UserRepositoryWrapper userRepositoryWrapper;

    @Override
    public void validateActiveUser(String username) {
        User user = userRepositoryWrapper.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found for username: {}", username);
                    return new UnauthorizedException("User not found for username: " + username);
                });

        if (user.getStatus() != UserStatus.ACTIVE) {
            logger.error("Inactive user attempted access: {} with status {}", username, user.getStatus());
            throw new UnauthorizedException("User is not active: " + username);
        }
    }
}

