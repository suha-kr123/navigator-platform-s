package framework.core.logger

import framework.config.BusinessContext
import framework.core.data.ApiContext
import framework.core.entity.ThirdPartyResponseLog
import framework.core.repository.ThirdPartyResponseLogRepository
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ThirdPartyRequestResponseLoggerImpl(
    private val thirdPartyResponseLogRepository: ThirdPartyResponseLogRepository
) : ThirdPartyRequestResponseLogger {

    override fun registerRequest(
        businessContext: BusinessContext,
        apiContext: ApiContext,
        requestMethod: HttpMethod,
        url: String,
        requestBody: String?
    ): UUID {
        val logEntry = ThirdPartyResponseLog(
            entityType = 1, // Default entity type
            entityId = businessContext.entityId,
            requestMethod = requestMethod.toString(),
            url = url,
            request = requestBody,
            businessPurpose = businessContext.businessPurpose,
            businessEntityName = businessContext.entityName,
            apiPurpose = apiContext.apiPurpose
        )

        val savedLog = thirdPartyResponseLogRepository.save(logEntry)
        return savedLog.id ?: UUID.randomUUID()
    }

    override fun registerThirdPartyRequest(
        businessContext: BusinessContext,
        apiContext: ApiContext,
        requestMethod: HttpMethod,
        url: String,
        requestBody: String?
    ): UUID {
        // For now, this is the same as registerRequest
        return registerRequest(businessContext, apiContext, requestMethod, url, requestBody)
    }

    override fun registerResponse(
        thirdPartyRequestId: UUID,
        responseBody: String,
        responseTimeInMs: Long,
        requestStatus: Int
    ) {
        val logEntry = thirdPartyResponseLogRepository.findById(thirdPartyRequestId).orElse(null)
        if (logEntry != null) {
            logEntry.response = responseBody
            logEntry.responseTimeInMs = responseTimeInMs
            logEntry.httpStatusCode = requestStatus
            thirdPartyResponseLogRepository.save(logEntry)
        }
    }
}
