package com.nivasafinance.features.person.dto

import com.nivasafinance.features.person.entity.PersonIdentifier
import com.nivasafinance.features.person.enum.IdentifierType
import java.util.UUID

data class PersonIdentifierData(
    val id: UUID,
    val personId: UUID,
    val identifier: String,
    val type: IdentifierType
) {
    companion object {
        fun fromEntity(entity: PersonIdentifier): PersonIdentifierData {
            return PersonIdentifierData(
                id = entity.id!!,
                personId = entity.personId,
                identifier = entity.identifier,
                type = entity.type
            )
        }
    }
}
