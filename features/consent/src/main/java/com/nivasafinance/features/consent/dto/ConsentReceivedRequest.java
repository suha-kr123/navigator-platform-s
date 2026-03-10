package com.nivasafinance.features.consent.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Request to create a consent record directly in RECEIVED state (e.g. when consent
 * is collected in customer web journey without an enquiry).
 */
@Value
@Builder
public class ConsentReceivedRequest {

    Long personId;

    String type;
}
