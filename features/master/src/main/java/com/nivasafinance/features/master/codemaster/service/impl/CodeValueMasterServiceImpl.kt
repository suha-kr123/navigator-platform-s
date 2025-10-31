package com.nivasafinance.features.master.codemaster.service.impl

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse
import com.nivasafinance.features.master.codemaster.dto.mapToCodeValueResponse
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService
import org.springframework.stereotype.Service

@Service
class CodeValueMasterServiceImpl(
    private val masterCodeValueRepositoryWrapper: MasterCodeValueRepositoryWrapper
) : CodeValueMasterService {

    override fun getCodeValueByKeyAndCodeKey(
        key: String,
        codeKey: String
    ): CodeValueResponse {
        return masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(key, codeKey).mapToCodeValueResponse()
    }
}
