package com.nivasafinance.features.lead.exception

import exception.BadRequestException

class InvalidLeadStatusException(status: String) : BadRequestException("Invalid lead status: $status")
