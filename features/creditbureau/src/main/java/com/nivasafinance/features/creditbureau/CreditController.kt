package com.nivasafinance.features.creditbureau

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CreditController {

    @GetMapping("/credit")
    fun creditCheck() : String{
        return "Tested OK"
    }
}