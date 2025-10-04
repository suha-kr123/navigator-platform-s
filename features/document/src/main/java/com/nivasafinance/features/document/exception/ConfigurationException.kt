package com.nivasafinance.features.document.exception

import com.nivasafinance.common.exception.BadRequestException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class ConfigurationException(message: String) : BadRequestException(message) {
    companion object {
        private val locale: Locale = LocaleContextHolder.getLocale()
        const val PROVIDER_NOT_CONFIGURED_KEY = "error.document.provider.not.configured"
    }

    constructor(providerType: String, messageSource: MessageSource) : this(
        messageSource.getMessage(PROVIDER_NOT_CONFIGURED_KEY, arrayOf(providerType), locale)
    )
}
