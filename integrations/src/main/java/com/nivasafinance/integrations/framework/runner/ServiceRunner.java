package com.nivasafinance.integrations.framework.runner;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.RunConfig;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationClientException;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationServerException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceRunner<S, A> {

    private final S primary;
    private final S fallback;
    private final int retries;

    public ServiceRunner(S primary, S fallback, int retries) {
        this.primary = primary;
        this.fallback = fallback;
        this.retries = retries;
    }

    public Object invokeService(
            String methodName,
            A argument,
            RunConfig runConfig,
            BusinessContext businessContext) throws NavigatorIntegrationClientException, NavigatorIntegrationServerException {
        NavigatorIntegrationServerException lastServerException = null;

        for (int i = 0; i < retries; i++) {
            try {
                Object result = tryPrimaryService(methodName, argument, runConfig, businessContext);
                if (result != null) {
                    return result;
                }
            } catch (NavigatorIntegrationServerException e) {
                lastServerException = e;
            }
        }

        return tryFallbackService(methodName, argument, runConfig, businessContext, lastServerException);
    }

    @SuppressWarnings("unchecked")
    private Object tryPrimaryService(
            String methodName,
            A argument,
            RunConfig runConfig,
            BusinessContext businessContext) throws NavigatorIntegrationServerException, NavigatorIntegrationClientException {
        try {
            Class<?> argumentClass = argument != null ? argument.getClass() : null;
            Method method = primary.getClass().getMethod(
                    methodName,
                    argumentClass,
                    runConfig.getPrimaryConfig().getClass(),
                    BusinessContext.class
            );
            return method.invoke(primary, argument, runConfig.getPrimaryConfig(), businessContext);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getTargetException();
            if (cause instanceof NavigatorIntegrationServerException) {
                throw new NavigatorIntegrationServerException(cause.getMessage());
            } else {
                throw new NavigatorIntegrationClientException(cause.getMessage());
            }
        } catch (NavigatorIntegrationServerException ex) {
            throw new NavigatorIntegrationServerException("Server error: " + ex.getMessage());
        } catch (NavigatorIntegrationClientException ex) {
            throw new NavigatorIntegrationClientException("Client error: " + ex.getMessage());
        } catch (Exception ex) {
            throw new NavigatorIntegrationClientException("Error invoking service: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Object tryFallbackService(
            String methodName,
            A argument,
            RunConfig runConfig,
            BusinessContext businessContext,
            NavigatorIntegrationServerException lastServerException) throws NavigatorIntegrationServerException, NavigatorIntegrationClientException {
        try {
            if (fallback == null || runConfig.getFallbackConfig() == null) {
                if (lastServerException != null) {
                    throw lastServerException;
                }
                return null;
            }
            Class<?> argumentClass = argument != null ? argument.getClass() : null;
            Method method = fallback.getClass().getMethod(
                    methodName,
                    argumentClass,
                    runConfig.getFallbackConfig().getClass(),
                    BusinessContext.class
            );
            return method.invoke(fallback, argument, runConfig.getFallbackConfig(), businessContext);
        } catch (NavigatorIntegrationServerException ex) {
            throw new NavigatorIntegrationServerException("Server error: " + ex.getMessage());
        } catch (NavigatorIntegrationClientException ex) {
            throw new NavigatorIntegrationClientException("Client error: " + ex.getMessage());
        } catch (Exception ex) {
            if (lastServerException != null) {
                throw lastServerException;
            }
            return null;
        }
    }
}

