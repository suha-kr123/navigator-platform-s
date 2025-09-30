package com.nivasafinance.features.master.codemaster.service.impl

import com.nivasafinance.features.master.codemaster.dto.CodeMasterListResponse
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse
import com.nivasafinance.features.master.codemaster.entity.MasterCode
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue
import com.nivasafinance.features.master.codemaster.exception.CodeMasterNotFoundException
import com.nivasafinance.features.master.codemaster.repository.MasterCodeRepositoryWrapper
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper
import com.nivasafinance.features.master.codemaster.service.CodeMasterService
import org.springframework.stereotype.Service

@Service
class CodeMasterServiceImpl(
    private val masterCodeRepositoryWrapper: MasterCodeRepositoryWrapper,
    private val masterCodeValueRepositoryWrapper: MasterCodeValueRepositoryWrapper
) : CodeMasterService {

    override fun getAllCodeValuesByCodeKey(codeKey: String, onlyActive: Boolean): CodeMasterListResponse {
        // Verify that the master code exists
        masterCodeRepositoryWrapper.findByKeyWithException(codeKey)

        val codeValues = if (onlyActive) {
            masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException(codeKey)
        } else {
            masterCodeValueRepositoryWrapper.findByCodeKeyWithException(codeKey)
        }
        val values = codeValues.map { mapToCodeValueResponse(it) }

        return CodeMasterListResponse(codeName = codeKey, values = values)
    }

    @Suppress("SwallowedException")
    override fun getCodeValueByKeyAndCodeKey(key: String, codeKey: String): Map<String, Any>? {
        return try {
            val masterCodeValue = masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(key, codeKey)
            masterCodeValue.value?.let { jsonData ->
                mapOf("default" to jsonData.default)
            }
        } catch (e: CodeMasterNotFoundException) {
            // Return null when code value is not found - this is intentional behavior
            // The exception is caught and handled by returning null instead of propagating
            null
        }
    }

    override fun getAllMasterCodes(onlyActive: Boolean): List<MasterCode> {
        return masterCodeRepositoryWrapper.findAllWithException()
    }

    override fun getMasterCodeChildrenWithValues(
        parentCodeKey: String,
        onlyActive: Boolean
    ): List<MasterCodeWithValuesResponse> {
        // Find the parent master code
        val parentMasterCode = masterCodeRepositoryWrapper.findByKeyWithException(parentCodeKey)

        // Find all children of the parent
        val parentId = checkNotNull(parentMasterCode.id) { "Parent master code ID is null" }
        val children = masterCodeRepositoryWrapper.findByParentIdWithException(parentId)

        // For each child, get their values and create the response
        return children.map { child ->
            val childValues = if (onlyActive) {
                masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException(child.key)
            } else {
                masterCodeValueRepositoryWrapper.findByCodeKeyWithException(child.key)
            }

            MasterCodeWithValuesResponse(
                id = child.id,
                key = child.key,
                name = child.name?.default.orEmpty(),
                description = child.description?.default.orEmpty(),
                isSystemDefined = child.isSystemDefined,
                parentId = child.parentId,
                values = childValues.map { mapToCodeValueResponse(it) }
            )
        }
    }

    private fun mapToCodeValueResponse(masterCodeValue: MasterCodeValue): CodeValueResponse {
        return CodeValueResponse(
            id = masterCodeValue.id,
            key = masterCodeValue.key,
            value = masterCodeValue.value?.default.orEmpty(),
            description = masterCodeValue.description?.default.orEmpty(),
            isActive = masterCodeValue.isActive
        )
    }
}
