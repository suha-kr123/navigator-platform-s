package com.nivasafinance.features.offices.service

import java.util.UUID

interface OfficeCodeFactory {
    fun generateOfficeCode(parentId: UUID?): String
}
