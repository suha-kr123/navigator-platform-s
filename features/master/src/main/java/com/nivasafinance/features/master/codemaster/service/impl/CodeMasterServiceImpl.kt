package com.nivasafinance.features.master.codemaster.service.impl

import com.nivasafinance.features.master.codemaster.dto.CodeMasterListResponse
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse
import com.nivasafinance.features.master.codemaster.entity.CodeValue
import com.nivasafinance.features.master.codemaster.exception.CodeMasterNotFoundException
import com.nivasafinance.features.master.codemaster.repository.CodeMasterRepositoryWrapper
import com.nivasafinance.features.master.codemaster.service.CodeMasterService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class CodeMasterServiceImpl(
    private val codeMasterRepositoryWrapper: CodeMasterRepositoryWrapper,
    private val messageSource: MessageSource
) : CodeMasterService {

    override fun getAllCodeMastersByCodeName(codeName: String): CodeMasterListResponse {
        val codeMasters = codeMasterRepositoryWrapper.findByCodeName(codeName)
        if (codeMasters.isEmpty()) {
            throw CodeMasterNotFoundException(codeName, messageSource)
        }
        val values = codeMasters.map { mapToCodeValueResponse(it.codeValue) }
        return CodeMasterListResponse(codeName = codeName, values = values)
    }

    override fun getCodeValueByKey(codeName: String, key: String): String? {
        val codeMaster = codeMasterRepositoryWrapper.findByCodeNameAndKey(codeName, key)
        return codeMaster.map { it.codeValue.value["default"] }.orElse(null)
    }

    private fun mapToCodeValueResponse(codeValue: CodeValue): CodeValueResponse {
        return CodeValueResponse(
            id = codeValue.id,
            key = codeValue.key,
            value = codeValue.value
        )
    }
}
