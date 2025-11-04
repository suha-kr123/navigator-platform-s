package framework.core.logger

import framework.config.BusinessContext
import framework.core.data.ApiContext
import org.springframework.http.HttpMethod
import java.util.UUID

interface ThirdPartyRequestResponseLogger {

    fun registerRequest(
        businessContext: BusinessContext,
        apiContext: ApiContext,
        requestMethod: HttpMethod,
        url: String,
        requestBody: String?
    ): UUID

    fun registerThirdPartyRequest(
        businessContext: BusinessContext,
        apiContext: ApiContext,
        requestMethod: HttpMethod,
        url: String,
        requestBody: String?
    ): UUID

    fun registerResponse(
        thirdPartyRequestId: UUID,
        responseBody: String,
        responseTimeInMs: Long,
        requestStatus: Int
    )
}
