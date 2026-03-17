package com.nivasafinance.common.resolver;

import java.util.Optional;

public interface UsernameResolver {

    Optional<String> resolveByEmail(String email);

    Optional<String> resolveByPhone(String phone);
}
