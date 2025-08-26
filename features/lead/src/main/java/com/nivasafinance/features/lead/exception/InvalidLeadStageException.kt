package com.nivasafinance.features.lead.exception

import exception.BadRequestException

class InvalidLeadStageException(stage: String) : BadRequestException("Invalid lead stage: $stage")
