package com.nivasafinance.features.offices.service.impl

import com.nivasafinance.common.base.BaseNavigatorService
import com.nivasafinance.features.offices.exception.OfficeNotFoundException
import com.nivasafinance.features.offices.repository.OfficeRepository
import com.nivasafinance.features.offices.service.OfficeCodeFactory
import org.springframework.stereotype.Service
import java.util.Locale
import java.util.UUID

@Service
class OfficeCodeFactoryImpl(
    private val officeRepository: OfficeRepository
) : OfficeCodeFactory, BaseNavigatorService() {

    override fun generateOfficeCode(parentId: UUID?): String {
        return if (parentId == null) {
            // Root office - generate next available root code (001, 002, 003, etc.)
            generateRootOfficeCode()
        } else {
            // Child office - generate code based on parent's code
            generateChildOfficeCode(parentId)
        }
    }

    private fun generateRootOfficeCode(): String {
        // Find the highest root office code and increment it
        val rootOffices = officeRepository.findByParentIdIsNullOrderByCodeDesc()
        val nextNumber = if (rootOffices.isEmpty()) {
            1
        } else {
            val lastCode = rootOffices.first().code
            lastCode.toIntOrNull()?.plus(1) ?: 1
        }
        return String.format(Locale.US, "%03d", nextNumber)
    }

    private fun generateChildOfficeCode(parentId: UUID): String {
        val parentOffice = officeRepository.findById(parentId).orElseThrow {
            OfficeNotFoundException(parentId, messageSource)
        }

        // Find all children of this parent and get the next sequence number
        val children = officeRepository.findByParentIdOrderByCodeDesc(parentId)
        val parentCode = parentOffice.code

        val nextNumber = if (children.isEmpty()) {
            1
        } else {
            val lastChildCode = children.first().code
            val lastSequence = lastChildCode.substringAfterLast(".").toIntOrNull() ?: 0
            lastSequence + 1
        }

        return "$parentCode.${String.format(Locale.US, "%03d", nextNumber)}"
    }
}
