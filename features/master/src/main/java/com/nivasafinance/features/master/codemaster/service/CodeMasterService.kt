package com.nivasafinance.features.master.codemaster.service

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse

interface CodeMasterService {

    fun getAllCodeValuesByCodeKey(codeKey: String, onlyActive: Boolean = true): List<CodeValueResponse>
    fun getMasterCodeChildrenWithValues(
        parentCodeKey: String,
        onlyActive: Boolean = true
    ): List<MasterCodeWithValuesResponse>
}
