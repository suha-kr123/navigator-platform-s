package data

import java.util.UUID

enum class IdentifierType {
    PAN, VOTER
}

data class Identifier(
    val id: String = UUID.randomUUID().toString(),
    var identifier: String?,
    var type: IdentifierType

)

data class IdentifierResponse(
    val id: UUID,
    val identifier: String?,
    val type: IdentifierType
)
