package com.nivasafinance.features.leadlender.enum

enum class LeadLenderStatus {
    PROPOSED,
    LOGGED_IN,
    REJECTED,
    ;

    val isInProgress: Boolean
        get() = this == PROPOSED || this == LOGGED_IN
    val isCompletedOrRejected: Boolean
        get() = this == REJECTED
}
