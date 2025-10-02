package com.nivasafinance.features.master.codemaster.service

import com.nivasafinance.features.master.codemaster.dto.CodeMasterListResponse
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse

interface CodeMasterService {

    fun getAllCodeValuesByCodeKey(codeKey: String, onlyActive: Boolean = true): CodeMasterListResponse
    fun getMasterCodeChildrenWithValues(
        parentCodeKey: String,
        onlyActive: Boolean = true
    ): List<MasterCodeWithValuesResponse>
}
