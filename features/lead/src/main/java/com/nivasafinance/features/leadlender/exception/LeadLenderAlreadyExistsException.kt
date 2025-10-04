package com.nivasafinance.features.leadlender.exception

import com.nivasafinance.common.exception.BadRequestException

class LeadLenderAlreadyExistsException(message: String) : BadRequestException(message)
