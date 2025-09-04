package com.nivasafinance.features.person.dto

import annotations.NoArg
import com.nivasafinance.features.person.enum.EmployerType
import com.nivasafinance.features.person.enum.EmploymentType
import java.math.BigDecimal
import java.util.UUID

@NoArg
data class EmploymentDetailsResponse(
    val employmentId: UUID?,
    val personId: UUID?,
    val employerName: String? = null,
    val employerType: EmployerType? = null,
    val jobTitle: String? = null,
    val department: String? = null,
    val employmentType: EmploymentType? = null,
    val location: String? = null,
    val salary: BigDecimal? = null,
    val documents: Map<String, Any>? = null,
    val extData: Map<String, Any>? = null
) {
    companion object {
        fun fromEmploymentDetails(
            employmentDetails: com.nivasafinance.features.person.entity.EmploymentDetails
        ): EmploymentDetailsResponse {
            return EmploymentDetailsResponse(
                employmentId = employmentDetails.employmentId,
                personId = employmentDetails.personId,
                employerName = employmentDetails.employerName,
                employerType = employmentDetails.employerType,
                jobTitle = employmentDetails.jobTitle,
                department = employmentDetails.department,
                employmentType = employmentDetails.employmentType,
                location = employmentDetails.location,
                salary = employmentDetails.salary,
                documents = employmentDetails.documents,
                extData = employmentDetails.extData
            )
        }
    }
}
