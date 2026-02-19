package com.nivasafinance.externals.customer.lead.service;

import com.nivasafinance.features.lead.dto.CurrentCustomerFormStepResponse;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.dto.PatchLeadRequest;
import com.nivasafinance.features.lead.dto.DocumentChecklistResponse;
import com.nivasafinance.features.lead.dto.IncomeObligationDetailsResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.dto.PropertyDetailsResponse;

import java.util.List;
import java.util.UUID;

public interface LeadExternalService {

    void patchLead(UUID leadIdentifier, PatchLeadRequest request);

    CurrentCustomerFormStepResponse getCurrentCustomerFormStep(UUID leadIdentifier);

    List<LeadContactResponse> getContacts(UUID leadIdentifier);

    PropertyDetailsResponse getPropertyDetails(UUID leadIdentifier);

    IncomeObligationDetailsResponse getIncomeObligationDetails(UUID leadIdentifier);

    DocumentChecklistResponse getDocumentChecklist(UUID leadIdentifier);

    PreliminaryDetailsResponse getPreliminaryDetails(UUID leadIdentifier);
}
