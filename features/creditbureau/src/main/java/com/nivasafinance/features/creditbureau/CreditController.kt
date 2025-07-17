package com.nivasafinance.features.creditbureau

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Locale

@RestController
class CreditController {

    @Autowired
    lateinit var messageSource: MessageSource
    final val locale: Locale = LocaleContextHolder.getLocale()

    @GetMapping("/credit")
    fun creditCheck(locale: Locale): String {
        return messageSource.getMessage("error.notfound", null, locale)
    }
}
