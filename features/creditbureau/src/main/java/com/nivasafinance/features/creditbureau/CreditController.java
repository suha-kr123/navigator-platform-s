package com.nivasafinance.features.creditbureau;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class CreditController {

    @Autowired
    private MessageSource messageSource;
    
    private final Locale locale = LocaleContextHolder.getLocale();

    @GetMapping("/credit")
    public String creditCheck(Locale locale) {
        return messageSource.getMessage("error.notfound", null, locale);
    }
}

