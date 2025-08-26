package com.nivasafinance.features.person.enum

import com.nivasafinance.features.person.exception.InvalidIdentifierTypeException

enum class IdentifierType {
    PAN,
    VOTER,
    AADHAR,
    PASSPORT,
    DRIVING_LICENSE;

    companion object {
        /**
         * Validates if the given string is a valid identifier type
         */
        fun isValid(identifierType: String?): Boolean {
            return identifierType?.let { type ->
                values().any { it.name == type }
            } ?: false
        }

        /**
         * Gets the identifier type from string, throws exception if invalid
         */
        fun fromString(identifierType: String): IdentifierType {
            return values().find { it.name == identifierType }
                ?: throw InvalidIdentifierTypeException.forEnum(identifierType)
        }

        /**
         * Gets all valid identifier types as a string
         */
        fun getValidTypes(): String = values().joinToString()
    }
}
