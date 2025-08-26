package com.nivasafinance.features.lead.exception

import exception.BadRequestException

class InvalidEnumValueException(
    field: String,
    value: String,
    validValues: List<String>
) : BadRequestException(
    "Invalid value '$value' for field '$field'. Valid values are: ${validValues.joinToString(", ")}"
)
