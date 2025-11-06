package com.nivasafinance.features.creditbureau;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class CreditController {

    @Autowired
    private MessageSource messageSource;
    
    @GetMapping("/credit")
    public String creditCheck(Locale locale) {
        return messageSource.getMessage("error.notfound", null, locale);
    }
}

