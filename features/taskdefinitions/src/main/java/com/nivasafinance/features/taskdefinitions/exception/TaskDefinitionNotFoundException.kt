package com.nivasafinance.features.taskdefinitions.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

class TaskDefinitionNotFoundException(id: UUID, messageSource: MessageSource) : ResourceNotFoundException("Task definition not found with id: $id")
