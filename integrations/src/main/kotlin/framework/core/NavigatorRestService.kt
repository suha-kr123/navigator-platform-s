package framework.core

import framework.core.data.IntegrationResponse
import framework.core.data.IntegrationRestRequest
import framework.core.logger.ThirdPartyRequestResponseLogger
import org.apache.commons.lang3.time.StopWatch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.Base64
import java.util.UUID

@Service
class NavigatorRestService @Autowired constructor(
    restTemplateBuilder: RestTemplateBuilder,
    private val thirdPartyRequestResponseLogger: ThirdPartyRequestResponseLogger
) {
    val restTemplate: RestTemplate = restTemplateBuilder.build()

    fun <T> doRestRequest(request: IntegrationRestRequest<T>): IntegrationResponse? {
        return doRestRequest(request, this.restTemplate)
    }

    @Suppress("ReturnCount")
    fun <T> doRestRequest(request: IntegrationRestRequest<T>, restTemplate: RestTemplate): IntegrationResponse? {
        val requestBody: String? = request.requestBody?.toString()
        val requestId = thirdPartyRequestResponseLogger.registerRequest(
            request.businessContext,
            request.apiContext,
            request.method,
            request.url,
            requestBody
        )
        val registerResponseBody = RegisterResponseBody(requestLogId = requestId, stopWatch = StopWatch())
        val stopWatch: StopWatch = registerResponseBody.stopWatch
        try {
            stopWatch.start()
            return performRestExchange(request, registerResponseBody, restTemplate)
        } catch (e: HttpStatusCodeException) {
            registerResponseBody.httpStatusCode = e.statusCode.value()
            registerResponseBody.responseString = e.responseBodyAsString
            return handleHttpStatusCodeException(e)
        } catch (e: RestClientException) {
            return IntegrationResponse.clientErrorResponse(e.message)
        } finally {
            if (stopWatch.isStarted) {
                stopWatch.stop()
            }
            val responseTimeInMs = stopWatch.time
            if (request.isResponseLoggable) {
                this.thirdPartyRequestResponseLogger.registerResponse(
                    registerResponseBody.requestLogId,
                    registerResponseBody.responseString.orEmpty(),
                    responseTimeInMs,
                    registerResponseBody.httpStatusCode
                )
            }
        }
    }

    private fun handleHttpStatusCodeException(e: HttpStatusCodeException): IntegrationResponse {
        return when {
            e.statusCode.is4xxClientError -> {
                IntegrationResponse.clientErrorResponse(
                    HttpStatus.valueOf(e.statusCode.value()),
                    e.responseBodyAsString
                )
            }
            e.statusCode.is5xxServerError || e.statusCode.is3xxRedirection -> {
                IntegrationResponse.serverErrorResponse(
                    HttpStatus.valueOf(e.statusCode.value()),
                    e.responseBodyAsString
                )
            }
            else -> {
                IntegrationResponse.clientErrorResponse(
                    HttpStatus.valueOf(e.statusCode.value()),
                    e.responseBodyAsString
                )
            }
        }
    }

    private fun <T> performRestExchange(
        request: IntegrationRestRequest<T>,
        registerResponseBody: RegisterResponseBody,
        restTemplate: RestTemplate
    ): IntegrationResponse? {
        val integrationResponse: IntegrationResponse?
        if (request.convertToBase64) {
            val response = restTemplate.exchange(
                request.getUri(),
                request.method,
                request.getHttpEntity(),
                ByteArray::class.java
            )
            registerResponseBody.stopWatch.stop()
            registerResponseBody.responseString = null
            registerResponseBody.httpStatusCode = response.statusCode.value()
            integrationResponse = IntegrationResponse.successResponse(
                HttpStatus.valueOf(response.statusCode.value()),
                Base64.getEncoder().encodeToString(response.getBody())
            )
        } else {
            val response = restTemplate.exchange(
                request.getUri(),
                request.method,
                request.getHttpEntity(),
                String::class.java
            )
            registerResponseBody.stopWatch.stop()
            registerResponseBody.responseString = response.getBody()
            registerResponseBody.httpStatusCode = response.statusCode.value()
            integrationResponse =
                IntegrationResponse.successResponse(HttpStatus.valueOf(response.statusCode.value()), response.getBody())
        }
        return integrationResponse
    }
}

data class RegisterResponseBody(
    var requestLogId: UUID,
    val stopWatch: StopWatch,
    var responseString: String? = null,
    var httpStatusCode: Int = HttpStatus.OK.value(),
)
