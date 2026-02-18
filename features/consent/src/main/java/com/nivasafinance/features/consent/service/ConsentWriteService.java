package com.nivasafinance.features.consent.service;

import com.nivasafinance.features.consent.dto.AcceptConsentRequest;
import com.nivasafinance.features.consent.dto.CreateAndSendConsent;
import com.nivasafinance.features.consent.dto.WithdrawConsentRequest;
import com.nivasafinance.features.consent.entity.Consent;

public interface ConsentWriteService {

    /**
     * Creates a consent (SENT), builds the link, publishes CB_CONSENT_SENT for notifications to send the link.
     */
    Consent createAndSendConsent(CreateAndSendConsent command);

    /**
     * Updates consent to RECEIVED. Caller (lead accept endpoint) must then call onConsentGranted.
     */
    void acceptConsent(AcceptConsentRequest command);

    /**
     * Updates consent to REQUEST_FOR_WITHDRAWAL.
     */
    void withdrawConsent(WithdrawConsentRequest command);
}
