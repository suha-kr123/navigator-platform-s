package com.nivasafinance.features.master.codemaster.repository

import com.nivasafinance.features.master.codemaster.entity.CodeMaster
import org.springframework.stereotype.Component
import java.util.*

@Component
class CodeMasterRepositoryWrapper(
    private val codeMasterRepository: CodeMasterRepository
) {

    fun findByCodeName(codeName: String): List<CodeMaster> {
        return codeMasterRepository.findByCodeName(codeName)
    }

    fun findByCodeNameAndKey(codeName: String, key: String): Optional<CodeMaster> {
        return codeMasterRepository.findByCodeNameAndKey(codeName, key)
    }

    fun saveAll(codeMasters: List<CodeMaster>): List<CodeMaster> {
        return codeMasterRepository.saveAll(codeMasters)
    }
}
