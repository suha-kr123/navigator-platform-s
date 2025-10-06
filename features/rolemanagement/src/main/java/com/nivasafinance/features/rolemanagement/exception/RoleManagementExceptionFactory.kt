package com.nivasafinance.features.rolemanagement.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.UUID

object RoleManagementExceptionFactory {

    fun notFound(entityKey: String, id: UUID, messageSource: MessageSource): RoleManagementNotFoundException {
        val messageKey = "error.rolemanagement.$entityKey.not.found"
        val message = ExceptionUtils.createLocalizedMessage(messageKey, arrayOf(id.toString()), messageSource)
        return RoleManagementNotFoundException(message)
    }
}

class RoleManagementNotFoundException(message: String) : RuntimeException(message)


