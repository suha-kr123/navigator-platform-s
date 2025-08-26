package com.nivasafinance.features.address.enum

import com.nivasafinance.features.address.exception.InvalidAddressTypeException

enum class AddressType {
    HOME, OFFICE;

    companion object {
        /**
         * Validates if the given string is a valid address type
         */
        fun isValid(addressType: String?): Boolean {
            return addressType?.let { type ->
                values().any { it.name == type }
            } ?: false
        }

        /**
         * Gets the address type from string, throws exception if invalid
         */
        fun fromString(addressType: String): AddressType {
            return values().find { it.name == addressType }
                ?: throw InvalidAddressTypeException.forEnum(addressType)
        }

        /**
         * Gets all valid address types as a string
         */
        fun getValidTypes(): String = values().joinToString()
    }
}
