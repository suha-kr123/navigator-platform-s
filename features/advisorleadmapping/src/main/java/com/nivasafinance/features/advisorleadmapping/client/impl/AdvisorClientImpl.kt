package com.nivasafinance.features.advisorleadmapping.client.impl

import com.nivasafinance.features.advisorleadmapping.client.AdvisorClient
import com.nivasafinance.features.advisorleadmapping.client.AdvisorInfo
import event.EventTopics
import event.GetAdvisorByMobileRequest
import event.GetAdvisorRequest
import event.ValidateAdvisorRequest
import event.impl.SpringEventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Suppress("NoWildcardImports", "NoUnusedImports", "NoTrailingSpaces", "WildcardImport", "VarCouldBeVal")
class AdvisorClientImpl : AdvisorClient {

    @Autowired
    private lateinit var eventService: SpringEventService

    override fun getAdvisor(id: UUID): AdvisorInfo {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val request = GetAdvisorRequest(
            requestId = requestId,
            advisorId = id,
            correlationId = correlationId
        )

        // In a real implementation, you'd wait for the response event
        // For now, we'll simulate a successful response
        val eventAdvisorInfo = AdvisorInfo(
            id = id,
            advisorCode = "ADV001",
            status = "ACTIVE",
            isEmployee = true
        )

        eventService.publishEvent(request, EventTopics.ADVISOR_GET_REQUEST)

        return com.nivasafinance.features.advisorleadmapping.client.AdvisorInfo(
            id = eventAdvisorInfo.id,
            advisorCode = eventAdvisorInfo.advisorCode,
            status = eventAdvisorInfo.status,
            isEmployee = eventAdvisorInfo.isEmployee
        )
    }

    override fun validateAdvisor(id: UUID): Boolean {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val request = ValidateAdvisorRequest(
            requestId = requestId,
            advisorId = id,
            correlationId = correlationId
        )

        eventService.publishEvent(request, EventTopics.ADVISOR_VALIDATE_REQUEST)

        // In a real implementation, you'd wait for the response event
        return true
    }

    override fun getAdvisorByMobile(mobileNumber: String): AdvisorInfo? {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val request = GetAdvisorByMobileRequest(
            requestId = requestId,
            mobileNumber = mobileNumber,
            correlationId = correlationId
        )

        eventService.publishEvent(request, EventTopics.ADVISOR_GET_BY_MOBILE_REQUEST)

        // In a real implementation, you'd wait for the response event
        return null
    }
}
