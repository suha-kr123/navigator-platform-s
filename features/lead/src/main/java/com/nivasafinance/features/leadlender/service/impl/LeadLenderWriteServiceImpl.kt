package com.nivasafinance.features.leadlender.service.impl

import com.nivasafinance.features.leadlender.dto.CreateLeadLenderRequest
import com.nivasafinance.features.leadlender.dto.CreateLeadLenderResponse
import com.nivasafinance.features.leadlender.dto.RejectLeadLenderRequest
import com.nivasafinance.features.leadlender.dto.RmDetails
import com.nivasafinance.features.leadlender.dto.UpdateLeadLenderRequest
import com.nivasafinance.features.leadlender.entity.LeadLender
import com.nivasafinance.features.leadlender.enum.LeadLenderStatus
import com.nivasafinance.features.leadlender.exception.InvalidLeadLenderStatusException
import com.nivasafinance.features.leadlender.exception.InvalidLenderOfficeException
import com.nivasafinance.features.leadlender.exception.LeadLenderAlreadyExistsException
import com.nivasafinance.features.leadlender.repository.LeadLenderRepositoryWrapper
import com.nivasafinance.features.leadlender.service.LeadLenderWriteService
import com.nivasafinance.features.lender.lender.service.LenderReadService
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeadLenderWriteServiceImpl(
    private val leadLenderRepositoryWrapper: LeadLenderRepositoryWrapper,
    private val lenderReadService: LenderReadService,
    private val lenderOfficeReadService: LenderOfficeReadService,
) : LeadLenderWriteService {

    @Transactional
    override fun createLeadLender(leadId: UUID, request: CreateLeadLenderRequest): CreateLeadLenderResponse {
        // First validate that the lender exists
        lenderReadService.getByKey(request.lenderKey)

        // Check if lead-lender relationship already exists
        try {
            val existingLeadLender = leadLenderRepositoryWrapper.findByLeadIdAndLenderKeyWithException(
                leadId,
                request.lenderKey
            )
            if (existingLeadLender.status.isInProgress) {
                throw LeadLenderAlreadyExistsException(
                    "Lead lender relationship already exists for lead: $leadId with lender: ${request.lenderKey}"
                )
            }
        } catch (_: Exception) {
        }

        val entity = LeadLender(
            leadId = leadId,
            lenderKey = request.lenderKey,
            status = LeadLenderStatus.PROPOSED
        )
        val savedLeadLender = leadLenderRepositoryWrapper.saveWithException(entity)
        return CreateLeadLenderResponse(id = savedLeadLender.id!!)
    }

    @Transactional
    override fun updateLeadLender(lenderId: UUID, request: UpdateLeadLenderRequest) {
        val existingEntity = leadLenderRepositoryWrapper.findByIdWithException(lenderId)

        // Check if already rejected
        if (existingEntity.status == LeadLenderStatus.REJECTED) {
            throw InvalidLeadLenderStatusException(
                "Cannot update rejected lead lender relationship"
            )
        }
        // Validate lender office if provided
        request.lenderOfficeKey?.let { officeKey ->
            validateOfficeForLender(officeKey, existingEntity)
            existingEntity.lenderOfficeKey = officeKey
        }
        request.loginId?.let {
            existingEntity.loginId = it
            existingEntity.status = LeadLenderStatus.LOGGED_IN
        }
        request.rmName?.let {
            val rmDetails = existingEntity.rmDetails ?: RmDetails()
            existingEntity.rmDetails = rmDetails.copy(name = it)
        }
        request.rmMobileNumber?.let {
            val rmDetails = existingEntity.rmDetails ?: RmDetails()
            existingEntity.rmDetails = rmDetails.copy(mobileNumber = it)
        }

        leadLenderRepositoryWrapper.saveWithException(existingEntity)
    }

    @Transactional
    override fun rejectLeadLender(lenderId: UUID, request: RejectLeadLenderRequest) {
        val existingEntity = leadLenderRepositoryWrapper.findByIdWithException(lenderId)

        // Check if already rejected
        if (existingEntity.status == LeadLenderStatus.REJECTED) {
            throw InvalidLeadLenderStatusException(
                "Lead lender relationship is already rejected. Current status: ${existingEntity.status}"
            )
        }

        // Update the fields directly instead of using copy() to preserve audit fields
        existingEntity.status = LeadLenderStatus.REJECTED
        existingEntity.rejectReason = request.rejectReason
        leadLenderRepositoryWrapper.saveWithException(existingEntity)
    }

    private fun validateOfficeForLender(
        officeKey: String,
        existingEntity: LeadLender
    ) {
        val lenderOffice = lenderOfficeReadService.getByKey(officeKey)

        // Validate that the lender office belongs to the same lender as the lead-lender relationship
        if (lenderOffice.lenderKey != existingEntity.lenderKey) {
            throw InvalidLenderOfficeException(
                "Invalid lender office key: $officeKey for lender: ${existingEntity.lenderKey}"
            )
        }
    }
}
