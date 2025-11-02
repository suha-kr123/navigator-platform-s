package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.CreateTrancheRequest;
import com.nivasafinance.features.lead.dto.UpdateCreditDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateDisbursementDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdatePreliminaryDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdatePropertyDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateProposedDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateTrancheRequest;

import java.util.UUID;

public interface LeadWriteService {
    CreateLeadResponse createLead(CreateLeadRequest request);
    void updatePreliminaryDetails(UUID leadIdentifier, UpdatePreliminaryDetailsRequest request);
    void updateCreditDetails(UUID leadIdentifier, UpdateCreditDetailsRequest request);
    void updateProposedDetails(UUID leadIdentifier, UpdateProposedDetailsRequest request);
    void updatePropertyDetails(UUID leadIdentifier, UpdatePropertyDetailsRequest request);
    void updateSourcingDetails(UUID leadIdentifier, UpdateSourcingDetailsRequest request);
    void updateDisbursementDetails(UUID leadIdentifier, UpdateDisbursementDetailsRequest request);
    void createTranche(UUID leadIdentifier, CreateTrancheRequest request);
    void updateTranche(UUID leadIdentifier, UUID trancheIdentifier, UpdateTrancheRequest request);
    void deleteTranche(UUID leadIdentifier, UUID trancheIdentifier);
}
