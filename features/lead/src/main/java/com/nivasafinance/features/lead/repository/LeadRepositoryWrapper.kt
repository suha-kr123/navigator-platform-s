package com.nivasafinance.features.lead.repository

import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class LeadRepositoryWrapper(
    private val leadRepository: LeadRepository,
    private val messageSource: MessageSource
) {

}
