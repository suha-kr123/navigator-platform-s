package com.nivasafinance.features.master.codemaster.service

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse

interface CodeValueMasterService {

    fun getCodeValueByKeyAndCodeKey(key: String, codeKey: String): CodeValueResponse
}
