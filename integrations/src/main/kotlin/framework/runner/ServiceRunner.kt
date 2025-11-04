package framework.runner

import framework.config.BusinessContext
import framework.core.data.RunConfig
import framework.core.exception.NavigatorIntegrationClientException
import framework.core.exception.NavigatorIntegrationServerException
import java.lang.reflect.InvocationTargetException

class ServiceRunner<S, A>(
    val primary: S,
    val fallback: S,
    var retries: Int,
) {
    @Throws(NavigatorIntegrationClientException::class, NavigatorIntegrationServerException::class)
    fun invokeService(
        methodName: String,
        argument: A?,
        runConfig: RunConfig,
        businessContext: BusinessContext
    ): Any? {
        val lastServerException: NavigatorIntegrationServerException? = null

        repeat(retries) {
            val result = tryPrimaryService(methodName, argument, runConfig, businessContext)
            if (result != null) return result
        }

        return tryFallbackService(methodName, argument, runConfig, businessContext, lastServerException)
    }

    @Suppress("ThrowsCount", "SwallowedException", "UnsafeCallOnNullableType")
    private fun tryPrimaryService(
        methodName: String,
        argument: A?,
        runConfig: RunConfig,
        businessContext: BusinessContext
    ): Any? {
        return try {
            val method = primary!!::class.java.getMethod(
                methodName,
                argument?.javaClass,
                runConfig.primaryConfig::class.java,
                BusinessContext::class.java
            )
            method.invoke(primary, argument, runConfig.primaryConfig, businessContext)
        } catch (ex: InvocationTargetException) {
            val cause = ex.targetException
            if (cause is NavigatorIntegrationServerException) {
                throw NavigatorIntegrationServerException(cause.message)
            } else {
                throw NavigatorIntegrationClientException(cause.message)
            }
        } catch (ex: NavigatorIntegrationServerException) {
            throw NavigatorIntegrationServerException("Server error: ${ex.message}")
        } catch (ex: NavigatorIntegrationClientException) {
            throw NavigatorIntegrationClientException("Client error: ${ex.message}")
        }
    }

    @Suppress("ThrowsCount", "SwallowedException", "UnsafeCallOnNullableType")
    private fun tryFallbackService(
        methodName: String,
        argument: A?,
        runConfig: RunConfig,
        businessContext: BusinessContext,
        lastServerException: NavigatorIntegrationServerException?
    ): Any? {
        return try {
            val method = fallback!!::class.java.getMethod(
                methodName,
                argument?.javaClass,
                runConfig.fallbackConfig?.javaClass,
                BusinessContext::class.java
            )
            method.invoke(fallback, argument, runConfig.fallbackConfig, businessContext)
        } catch (ex: NavigatorIntegrationServerException) {
            throw NavigatorIntegrationServerException("Server error: ${ex.message}")
        } catch (ex: NavigatorIntegrationClientException) {
            throw NavigatorIntegrationClientException("Client error: ${ex.message}")
        } catch (_: Exception) {
            lastServerException?.let { throw it }
            null
        }
    }
}
