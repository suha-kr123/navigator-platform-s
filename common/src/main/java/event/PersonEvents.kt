package event

import java.util.UUID

data class GetPersonRequest(
    val requestId: String,
    val personId: UUID,
    val correlationId: String
)

data class GetPersonByMobileRequest(
    val requestId: String,
    val mobileNumber: String,
    val correlationId: String
)

data class GetPersonResponse(
    val requestId: String,
    val correlationId: String,
    val success: Boolean,
    val personInfo: PersonInfo? = null,
    val error: String? = null
)

data class GetPersonByMobileResponse(
    val requestId: String,
    val correlationId: String,
    val success: Boolean,
    val personInfo: PersonInfo? = null,
    val error: String? = null
)

data class PersonInfo(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val mobileNumbers: List<MobileNumber>,
    val emailAddresses: List<EmailAddress>,
    val dateOfBirth: String?,
    val gender: String?,
    val status: String
)

data class MobileNumber(
    val number: String,
    val isPrimary: Boolean,
    val isVerified: Boolean
)

data class EmailAddress(
    val email: String,
    val isPrimary: Boolean,
    val isVerified: Boolean
)
