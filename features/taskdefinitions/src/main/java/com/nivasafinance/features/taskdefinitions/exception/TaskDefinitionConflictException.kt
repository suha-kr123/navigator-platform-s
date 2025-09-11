package com.nivasafinance.features.taskdefinitions.exception

import exception.ResourceConflictException
import org.springframework.context.MessageSource

class TaskDefinitionConflictException(key: String, messageSource: MessageSource) : ResourceConflictException("Task definition with key '$key' already exists")
