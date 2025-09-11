package com.nivasafinance.features.advisor.event

import com.nivasafinance.features.advisor.service.AdvisorService
import event.AdvisorInfo
import event.EventTopics
import event.GetAdvisorByMobileRequest
import event.GetAdvisorByMobileResponse
import event.GetAdvisorRequest
import event.GetAdvisorResponse
import event.ValidateAdvisorRequest
import event.ValidateAdvisorResponse
import event.impl.SpringEventService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@Suppress("TooGenericExceptionCaught")
class AdvisorEventHandler(
    private val advisorService: AdvisorService,
    private val eventService: SpringEventService
) {

    @KafkaListener(topics = [EventTopics.ADVISOR_GET_REQUEST])
    fun handleGetAdvisorRequest(request: GetAdvisorRequest) {
        try {
            val advisor = advisorService.getAdvisor(request.advisorId)
            val response = GetAdvisorResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                success = true,
                advisorInfo = AdvisorInfo(
                    id = advisor.id,
                    advisorCode = advisor.advisorCode,
                    status = advisor.status?.name ?: "UNKNOWN",
                    isEmployee = advisor.isEmployee
                )
            )
            eventService.publishEvent(response, EventTopics.ADVISOR_GET_RESPONSE)
        } catch (e: Exception) {
            val response = GetAdvisorResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                success = false,
                error = e.message
            )
            eventService.publishEvent(response, EventTopics.ADVISOR_GET_RESPONSE)
        }
    }

    @KafkaListener(topics = [EventTopics.ADVISOR_VALIDATE_REQUEST])
    fun handleValidateAdvisorRequest(request: ValidateAdvisorRequest) {
        try {
            advisorService.getAdvisor(request.advisorId)
            val response = ValidateAdvisorResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                isValid = true
            )
            eventService.publishEvent(response, EventTopics.ADVISOR_VALIDATE_RESPONSE)
        } catch (e: Exception) {
            val response = ValidateAdvisorResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                isValid = false,
                error = e.message
            )
            eventService.publishEvent(response, EventTopics.ADVISOR_VALIDATE_RESPONSE)
        }
    }

    @KafkaListener(topics = [EventTopics.ADVISOR_GET_BY_MOBILE_REQUEST])
    fun handleGetAdvisorByMobileRequest(request: GetAdvisorByMobileRequest) {
        try {
            val advisor = advisorService.getAdvisorByMobileNo(request.mobileNumber)
            val response = GetAdvisorByMobileResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                success = true,
                advisorInfo = AdvisorInfo(
                    id = advisor.id,
                    advisorCode = advisor.advisorCode,
                    status = advisor.status?.name ?: "UNKNOWN",
                    isEmployee = advisor.isEmployee
                )
            )
            eventService.publishEvent(response, EventTopics.ADVISOR_GET_BY_MOBILE_RESPONSE)
        } catch (e: Exception) {
            val response = GetAdvisorByMobileResponse(
                requestId = request.requestId,
                correlationId = request.correlationId,
                success = false,
                error = e.message
            )
            eventService.publishEvent(response, EventTopics.ADVISOR_GET_BY_MOBILE_RESPONSE)
        }
    }
}
