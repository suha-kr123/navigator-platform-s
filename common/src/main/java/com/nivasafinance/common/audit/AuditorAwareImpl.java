package com.nivasafinance.common.audit;

import com.nivasafinance.common.context.UserContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    private static final int MAX_USERNAME_LENGTH = 255;
    private static final String SYSTEM_USERNAME = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        String username = UserContext.getUsername();
        
        // Use "system" as fallback when username is null (e.g., in async handlers)
        if (username == null || username.isBlank()) {
            username = SYSTEM_USERNAME;
        }
        
        // Truncate username to 255 characters to prevent database issues
        if (username.length() > MAX_USERNAME_LENGTH) {
            username = username.substring(0, MAX_USERNAME_LENGTH);
        }
        
        return Optional.of(username);
    }
}

