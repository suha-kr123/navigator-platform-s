package com.nivasafinance.features.consent.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Command for create-and-send consent. leadIdentifier and contactIdentifier are used to build
 * the consent link so the external UI can call the accept API.
 */
@Value
@Builder
public class CreateAndSendConsent {

    Long personId;

    String type;

    UUID enquiryIdentifier;

    UUID leadIdentifier;

    UUID contactIdentifier;

    String entityType;

    Long entityId;

    String recipientPhone;
}
