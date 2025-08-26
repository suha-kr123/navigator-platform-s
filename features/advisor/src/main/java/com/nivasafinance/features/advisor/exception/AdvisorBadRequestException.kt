package com.nivasafinance.features.advisor.exception

import exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class AdvisorBadRequestException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val ADVISOR_FETCH_ERROR_KEY = "error.advisor.fetch.failed"
        const val ADVISOR_PROCESS_ERROR_KEY = "error.advisor.process.failed"
        const val ADVISOR_RETRIEVE_ERROR_KEY = "error.advisor.retrieve.failed"
        const val ADVISOR_PERSON_DETAILS_ERROR_KEY = "error.advisor.person.details.failed"
        const val ADVISOR_NOT_FOUND_BY_ID_KEY = "error.advisor.not.found.by.id"
        const val ADVISOR_NOT_FOUND_BY_MOBILE_KEY = "error.advisor.not.found.by.mobile"
        const val ADVISOR_LEAD_MAPPING_FETCH_ERROR_KEY = "error.advisor.lead.mapping.fetch.failed"
        const val ADVISOR_LEAD_MAPPING_PROCESS_ERROR_KEY = "error.advisor.lead.mapping.process.failed"
        const val ADVISOR_LEAD_MAPPING_RETRIEVE_ERROR_KEY = "error.advisor.lead.mapping.retrieve.failed"
        const val ADVISOR_LEAD_MAPPING_SAVE_ERROR_KEY = "error.advisor.lead.mapping.save.failed"
        const val ADVISOR_LEAD_MAPPING_UPDATE_ERROR_KEY = "error.advisor.lead.mapping.update.failed"
        const val ADVISOR_LEAD_MAPPING_DELETE_ERROR_KEY = "error.advisor.lead.mapping.delete.failed"
        const val ADVISOR_LEAD_MAPPING_RETRIEVE_EXISTING_ERROR_KEY = "error.advisor.lead.mapping.retrieve.existing"
        const val ADVISOR_LEAD_CREATE_ERROR_KEY = "error.advisor.lead.create.failed"
        const val ADVISOR_LEAD_ADD_ERROR_KEY = "error.advisor.lead.add.failed"
        const val ADVISOR_PERSON_ID_REQUIRED_KEY = "error.advisor.person.id.required"
    }

    constructor(messageKey: String, args: Array<Any>, messageSource: MessageSource) : this(
        messageSource.getMessage(messageKey, args, locale)
    )

    constructor(messageKey: String, messageSource: MessageSource) : this(
        messageSource.getMessage(messageKey, null, locale)
    )
}
