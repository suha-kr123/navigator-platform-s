package com.nivasafinance.externals.customer.lead.service;

import com.nivasafinance.features.lead.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadExternalService {

    void patchLead(UUID leadIdentifier, PatchLeadRequest request);

    LeadResponse getLeadByIdentifier(UUID leadIdentifier);

    CurrentCustomerFormStepResponse getCurrentCustomerFormStep(UUID leadIdentifier);

    List<LeadContactResponse> getContacts(UUID leadIdentifier);

    PropertyDetailsResponse getPropertyDetails(UUID leadIdentifier);

    IncomeObligationDetailsResponse getIncomeObligationDetails(UUID leadIdentifier);

    DocumentChecklistResponse getDocumentChecklist(UUID leadIdentifier);

    PreliminaryDetailsResponse getPreliminaryDetails(UUID leadIdentifier);

    LeadBREResultExecuteResponse executeEligibility(UUID leadIdentifier);

    Optional<LeadEligibilityResponse> getLatestEligibility(UUID leadIdentifier);
}
