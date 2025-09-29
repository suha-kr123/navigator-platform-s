package com.nivasafinance.features.master.codemaster.service

import com.nivasafinance.features.master.codemaster.dto.CodeMasterListResponse
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse
import com.nivasafinance.features.master.codemaster.entity.MasterCode

interface CodeMasterService {

    fun getAllCodeValuesByCodeKey(codeKey: String, onlyActive: Boolean = true): CodeMasterListResponse

    fun getCodeValueByKeyAndCodeKey(key: String, codeKey: String): Map<String, Any>?

    fun getAllMasterCodes(onlyActive: Boolean = true): List<MasterCode>

    fun getMasterCodeChildrenWithValues(
        parentCodeKey: String,
        onlyActive: Boolean = true
    ): List<MasterCodeWithValuesResponse>
}
