package com.nivasafinance.common.base

import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource

open class BaseNavigatorService {

    @Autowired
    lateinit var modelMapper: ModelMapper

    @Autowired
    lateinit var messageSource: MessageSource
}
