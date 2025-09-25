package com.nivasafinance.features.master.codemaster.service

import com.nivasafinance.features.master.codemaster.dto.CodeMasterListResponse

interface CodeMasterService {

    fun getAllCodeMastersByCodeName(codeName: String): CodeMasterListResponse

    fun getCodeValueByKey(codeName: String, key: String): String?
}
