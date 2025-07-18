package data

enum class IdentifierType {
    PAN, VOTER
}

data class Identifier(
    val identifier: String,
    val type: IdentifierType

)
