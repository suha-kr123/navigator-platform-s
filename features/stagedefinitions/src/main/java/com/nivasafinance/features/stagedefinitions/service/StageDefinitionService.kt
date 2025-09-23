package com.nivasafinance.features.stagedefinitions.service

import com.nivasafinance.features.stagedefinitions.dto.StageOutcomesResponse

interface StageDefinitionService {
    fun getStageOutcomesByKey(key: String): StageOutcomesResponse
}
