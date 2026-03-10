package com.nivasafinance.features.consent.service;

import com.nivasafinance.features.consent.dto.AcceptConsentRequest;
import com.nivasafinance.features.consent.dto.ConsentReceivedRequest;
import com.nivasafinance.features.consent.dto.CreateAndSendConsent;
import com.nivasafinance.features.consent.dto.ResendConsentRequest;
import com.nivasafinance.features.consent.dto.WithdrawConsentRequest;
import com.nivasafinance.features.consent.entity.Consent;

public interface ConsentWriteService {

    /**
     * Creates a consent (SENT), builds the link, publishes CB_CONSENT_SENT for notifications to send the link.
     */
    Consent createAndSendConsent(CreateAndSendConsent command);

    /**
     * Creates a consent record directly in RECEIVED state. No link is sent; used when consent
     * is collected in-app (e.g. customer web journey) without an enquiry.
     */
    Consent createConsentReceived(ConsentReceivedRequest request);

    /**
     * Updates consent to RECEIVED. Caller (lead accept endpoint) must then call onConsentGranted.
     */
    void acceptConsent(AcceptConsentRequest command);

    /**
     * Resends the consent link for an existing consent (status must be SENT). No new consent row is created.
     */
    void resendConsent(ResendConsentRequest request);

    /**
     * Updates consent to REQUEST_FOR_WITHDRAWAL.
     */
    void withdrawConsent(WithdrawConsentRequest command);
}
