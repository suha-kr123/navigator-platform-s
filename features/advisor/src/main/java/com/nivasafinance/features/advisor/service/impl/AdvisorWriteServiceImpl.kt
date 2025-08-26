package com.nivasafinance.features.advisor.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorData
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.exception.AdvisorConflictException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.advisor.service.AdvisorWriteService
import com.nivasafinance.features.person.service.PersonReadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdvisorWriteServiceImpl(
    private val advisorRepository: AdvisorRepository,
    private val personReadService: PersonReadService
) : AdvisorWriteService, BaseNavigatorService() {

    @Transactional
    override fun createAdvisorData(request: AdvisorCreateRequest): AdvisorData {
        val personId = request.personId!!
        personReadService.getPerson(personId)

        val existingAdvisor = advisorRepository.findByPersonId(personId)
        if (existingAdvisor != null) {
            throw AdvisorConflictException(personId, messageSource)
        }

        val advisor = Advisor(
            personId = personId,
            advisorCode = request.advisorCode,
            isEmployee = request.isEmployee,
            status = AdvisorStatus.CREATED,
            remarks = request.remarks,
            rejectionReason = request.rejectionReason,
            advisorFeedback = request.advisorFeedback,
            welcomeKitSent = request.welcomeKitSent,
            attendedAdvisorMeeting = request.attendedAdvisorMeeting,
            extData = request.extData
        )

        val savedAdvisor = advisorRepository.save(advisor)
        return AdvisorData.fromEntity(savedAdvisor)
    }

    @Transactional
    override fun updateAdvisorData(id: UUID, request: AdvisorUpdateRequest): AdvisorData {
        val existingAdvisor = advisorRepository.findById(id).orElseThrow {
            AdvisorNotFoundException(id, messageSource)
        }

        val updatedAdvisor = Advisor(
            id = existingAdvisor.id,
            personId = existingAdvisor.personId,
            advisorCode = request.advisorCode ?: existingAdvisor.advisorCode,
            isEmployee = request.isEmployee ?: existingAdvisor.isEmployee,
            status = request.status ?: existingAdvisor.status,
            isExperiencedDsa = existingAdvisor.isExperiencedDsa,
            remarks = request.remarks,
            rejectionReason = request.rejectionReason,
            advisorFeedback = request.advisorFeedback,
            welcomeKitSent = request.welcomeKitSent ?: existingAdvisor.welcomeKitSent,
            attendedAdvisorMeeting = request.attendedAdvisorMeeting ?: existingAdvisor.attendedAdvisorMeeting,
            extData = request.extData ?: existingAdvisor.extData
        )

        val savedAdvisor = advisorRepository.save(updatedAdvisor)
        return AdvisorData.fromEntity(savedAdvisor)
    }

    @Transactional
    override fun deleteAdvisor(id: UUID) {
        val advisor = advisorRepository.findById(id).orElseThrow {
            AdvisorNotFoundException(id, messageSource)
        }
        advisorRepository.delete(advisor)
    }
}
