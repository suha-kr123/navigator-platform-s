package com.nivasafinance.features.usermanagement.resolver;

import com.nivasafinance.common.resolver.UsernameResolver;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsernameResolverImpl implements UsernameResolver {

    private final UserReadService userReadService;

    @Override
    public Optional<String> resolveByEmail(String email) {
        return userReadService.resolveUsernameByEmail(email);
    }

    @Override
    public Optional<String> resolveByPhone(String phone) {
        return userReadService.resolveUsernameByPhone(phone);
    }
}
