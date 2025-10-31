package com.nivasafinance.common.audit;

import com.nivasafinance.security.context.UserContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    private static final int MAX_USERNAME_LENGTH = 255;

    @Override
    public Optional<String> getCurrentAuditor() {
        String username = UserContext.getUserInfo() != null 
                ? UserContext.getUserInfo().getUsername() 
                : "system";
        // Truncate username to 255 characters to prevent database issues
        if (username.length() > MAX_USERNAME_LENGTH) {
            username = username.substring(0, MAX_USERNAME_LENGTH);
        }
        return Optional.of(username);
    }
}

