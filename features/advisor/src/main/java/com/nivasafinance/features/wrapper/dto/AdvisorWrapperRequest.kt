package com.nivasafinance.features.wrapper.dto

data class AdvisorWrapperRequest(
    val personData: PersonData,
    val advisorData: AdvisorData? = null
) {
    data class PersonData(
        val firstName: String? = null,
        val middleName: String? = null,
        val lastName: String? = null,
        val mobileNumbers: List<MobileNumberDetails> = emptyList(),
        val email: String? = null,
        val dateOfBirth: String? = null,
        val gender: String? = null,
        val addresses: List<AddressData> = emptyList(),
        val identifiers: List<IdentifierData> = emptyList()
    ) {
        data class MobileNumberDetails(
            val number: String? = null,
            val isPrimary: Boolean? = null
        )

        data class AddressData(
            val addressOne: String? = null,
            val addressTwo: String? = null,
            val landmark: String? = null,
            val district: String? = null,
            val state: String? = null,
            val pincode: String,
            val addressSource: String? = null,
            val addressType: String
        )

        data class IdentifierData(
            val identifier: String,
            val type: String
        )
    }

    data class AdvisorData(
        val advisorCode: String? = null,
        val remarks: String? = null
    )
}
