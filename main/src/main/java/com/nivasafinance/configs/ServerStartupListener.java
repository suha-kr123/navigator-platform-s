package com.nivasafinance.configs;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ServerStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment environment;

    public ServerStartupListener(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        
        System.out.println();
        System.out.println("Server started on port " + port + contextPath);
        System.out.println("Hit Enter to stop the server...");
    }
}

