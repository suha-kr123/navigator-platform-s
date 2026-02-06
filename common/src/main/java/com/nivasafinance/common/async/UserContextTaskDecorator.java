package com.nivasafinance.common.async;

import com.nivasafinance.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

/**
 * TaskDecorator that propagates UserContext to async threads.
 * This ensures that the username from the parent thread is available
 * in the async thread's ThreadLocal context.
 */
@Slf4j
public class UserContextTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // Capture the username from the current thread (parent thread)
        String username = UserContext.getUsername();
        
        // Return a wrapped runnable that sets the context before execution
        return () -> {
            try {
                // Set the username in the async thread's ThreadLocal
                if (username != null) {
                    UserContext.setUsername(username);
                    log.debug("UserContext propagated to async thread: {}", username);
                }
                
                // Execute the actual task
                runnable.run();
            } finally {
                // Always clear the context after execution to prevent memory leaks
                UserContext.clear();
                log.debug("UserContext cleared from async thread");
            }
        };
    }
}
