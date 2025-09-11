package com.nivasafinance.features.tasks.exception

import exception.ResourceNotFoundException
import java.util.UUID

class LeadTaskNotFoundException(id: UUID) : ResourceNotFoundException("Lead task not found with id: $id")
