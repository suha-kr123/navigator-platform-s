package com.nivasafinance.common.resolver;

import java.util.Optional;

public interface UsernameResolver {

    Optional<String> resolveByEmailOrPhone(String email, String phone);
}
