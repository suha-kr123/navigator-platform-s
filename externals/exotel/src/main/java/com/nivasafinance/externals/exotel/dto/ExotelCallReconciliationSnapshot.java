package com.nivasafinance.externals.exotel.dto;

import com.nivasafinance.features.call.enums.CallStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExotelCallReconciliationSnapshot {
    CallStatus status;
    /** Duration in seconds from Exotel. Null means do not apply a duration update. */
    Long durationSeconds;
}
