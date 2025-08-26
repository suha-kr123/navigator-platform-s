package com.nivasafinance.features.advisor.dto

import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import java.util.UUID

data class AdvisorData(
    val id: UUID?,
    val personId: UUID,
    val advisorCode: String,
    val isEmployee: Boolean,
    val status: AdvisorStatus,
    val isExperiencedDsa: Boolean,
    val remarks: String?,
    val rejectionReason: String?,
    val advisorFeedback: String?,
    val welcomeKitSent: Boolean,
    val attendedAdvisorMeeting: Boolean,
    val extData: Map<String, Any>?
) {
    companion object {
        fun fromEntity(advisor: Advisor): AdvisorData {
            return AdvisorData(
                id = advisor.id,
                personId = advisor.personId,
                advisorCode = advisor.advisorCode,
                isEmployee = advisor.isEmployee,
                status = advisor.status,
                isExperiencedDsa = advisor.isExperiencedDsa,
                remarks = advisor.remarks,
                rejectionReason = advisor.rejectionReason,
                advisorFeedback = advisor.advisorFeedback,
                welcomeKitSent = advisor.welcomeKitSent,
                attendedAdvisorMeeting = advisor.attendedAdvisorMeeting,
                extData = advisor.extData
            )
        }
    }
}
