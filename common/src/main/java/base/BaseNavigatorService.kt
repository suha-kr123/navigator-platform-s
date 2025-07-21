package base

import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource

abstract class BaseNavigatorService() {

    @Autowired
    lateinit var modelMapper: ModelMapper;

    @Autowired
    lateinit var messageSource: MessageSource
}