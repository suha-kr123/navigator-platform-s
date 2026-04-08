package com.nivasafinance.integrations.framework.runner;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.RunConfig;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationClientException;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRunnerTest {

    private ThirdPartyConfig primaryConfig;
    private ThirdPartyConfig fallbackConfig;
    private BusinessContext businessContext;

    @BeforeEach
    void setUp() {
        primaryConfig = new ThirdPartyConfig(1L, "primary", "provider", Map.of());
        fallbackConfig = new ThirdPartyConfig(2L, "fallback", "provider", Map.of());
        businessContext = new BusinessContext("LOAN", 100L, "TEST");
    }

    // ── primary success ──

    @Test
    void invokeService_primaryReturnsResult_returnsImmediately() {
        ServiceRunner<SuccessService, String> runner = new ServiceRunner<>(
                new SuccessService(), null, 1);
        RunConfig runConfig = new RunConfig(primaryConfig, null, 1);

        Object result = runner.invokeService("execute", "arg", runConfig, businessContext);

        assertEquals("primary-result", result,
                "Should return the primary service result on success");
    }

    @Test
    void invokeService_primaryReturnsNull_noFallback_returnsNull() {
        ServiceRunner<NullReturnService, String> runner = new ServiceRunner<>(
                new NullReturnService(), null, 1);
        RunConfig runConfig = new RunConfig(primaryConfig, null, 1);

        Object result = runner.invokeService("execute", "arg", runConfig, businessContext);

        assertNull(result, "Should return null when primary returns null and no fallback is configured");
    }

    @Test
    void invokeService_primaryReturnsNull_withFallback_returnsFallbackResult() {
        ServiceRunner<Object, String> runner = new ServiceRunner<>(
                new NullReturnService(), new FallbackSuccessService(), 1);
        RunConfig runConfig = new RunConfig(primaryConfig, fallbackConfig, 1);

        Object result = runner.invokeService("execute", "arg", runConfig, businessContext);

        assertEquals("fallback-result", result,
                "Should return fallback result when primary returns null");
    }

    // ── retry logic ──

    @Test
    void invokeService_primaryServerException_retrySucceeds_returnsResult() {
        ServiceRunner<RetryableService, String> runner = new ServiceRunner<>(
                new RetryableService(), null, 2);
        RunConfig runConfig = new RunConfig(primaryConfig, null, 2);

        Object result = runner.invokeService("execute", "arg", runConfig, businessContext);

        assertEquals("retry-result", result,
                "Should return result after a successful retry");
    }

    @Test
    void invokeService_primaryServerException_exhaustsRetries_fallbackSucceeds() {
        ServiceRunner<Object, String> runner = new ServiceRunner<>(
                new ServerErrorService(), new FallbackSuccessService(), 2);
        RunConfig runConfig = new RunConfig(primaryConfig, fallbackConfig, 2);

        Object result = runner.invokeService("execute", "arg", runConfig, businessContext);

        assertEquals("fallback-result", result,
                "Should return fallback result after all primary retries are exhausted");
    }

    // ── no fallback ──

    @Test
    void invokeService_primaryServerException_noFallback_rethrowsServerException() {
        ServiceRunner<ServerErrorService, String> runner = new ServiceRunner<>(
                new ServerErrorService(), null, 1);
        RunConfig runConfig = new RunConfig(primaryConfig, null, 1);

        assertThrows(NavigatorIntegrationServerException.class,
                () -> runner.invokeService("execute", "arg", runConfig, businessContext),
                "Should rethrow server exception when no fallback is available");
    }

    // ── client exception ──

    @Test
    void invokeService_primaryClientException_throwsClientException() {
        ServiceRunner<Object, String> runner = new ServiceRunner<>(
                new ClientErrorService(), new FallbackSuccessService(), 2);
        RunConfig runConfig = new RunConfig(primaryConfig, fallbackConfig, 2);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> runner.invokeService("execute", "arg", runConfig, businessContext),
                "Client exceptions should propagate immediately without retry");
    }

    // ── fallback failures ──

    @Test
    void invokeService_primaryAndFallbackBothFail_throwsServerException() {
        ServiceRunner<Object, String> runner = new ServiceRunner<>(
                new ServerErrorService(), new FallbackServerErrorService(), 1);
        RunConfig runConfig = new RunConfig(primaryConfig, fallbackConfig, 1);

        assertThrows(NavigatorIntegrationServerException.class,
                () -> runner.invokeService("execute", "arg", runConfig, businessContext),
                "Should throw server exception when both primary and fallback fail");
    }

    // ── method not found ──

    @Test
    void invokeService_methodNotFound_throwsClientException() {
        ServiceRunner<SuccessService, String> runner = new ServiceRunner<>(
                new SuccessService(), null, 1);
        RunConfig runConfig = new RunConfig(primaryConfig, null, 1);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> runner.invokeService("nonExistentMethod", "arg", runConfig, businessContext),
                "Should throw client exception when the method is not found via reflection");
    }

    // ── test helper services ──

    public static class SuccessService {
        public Object execute(String arg, ThirdPartyConfig config, BusinessContext ctx) {
            return "primary-result";
        }
    }

    public static class NullReturnService {
        public Object execute(String arg, ThirdPartyConfig config, BusinessContext ctx) {
            return null;
        }
    }

    public static class ServerErrorService {
        public Object execute(String arg, ThirdPartyConfig config, BusinessContext ctx)
                throws NavigatorIntegrationServerException {
            throw new NavigatorIntegrationServerException("server error");
        }
    }

    public static class ClientErrorService {
        public Object execute(String arg, ThirdPartyConfig config, BusinessContext ctx)
                throws NavigatorIntegrationClientException {
            throw new NavigatorIntegrationClientException("client error");
        }
    }

    public static class RetryableService {
        private int callCount = 0;

        public Object execute(String arg, ThirdPartyConfig config, BusinessContext ctx)
                throws NavigatorIntegrationServerException {
            callCount++;
            if (callCount <= 1) {
                throw new NavigatorIntegrationServerException("server error");
            }
            return "retry-result";
        }
    }

    public static class FallbackSuccessService {
        public Object execute(String arg, ThirdPartyConfig config, BusinessContext ctx) {
            return "fallback-result";
        }
    }

    public static class FallbackServerErrorService {
        public Object execute(String arg, ThirdPartyConfig config, BusinessContext ctx)
                throws NavigatorIntegrationServerException {
            throw new NavigatorIntegrationServerException("fallback server error");
        }
    }
}
