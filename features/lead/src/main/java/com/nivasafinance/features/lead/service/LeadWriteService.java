package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.CreateTrancheRequest;
import com.nivasafinance.features.lead.dto.DropoffLeadRequest;
import com.nivasafinance.features.lead.dto.OnholdLeadRequest;
import com.nivasafinance.features.lead.dto.RejectLeadRequest;
import com.nivasafinance.features.lead.dto.UpdateCreditDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateDisbursementDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateLeadRequest;
import com.nivasafinance.features.lead.dto.UpdatePreliminaryDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdatePropertyDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateProposedDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateTrancheRequest;
import com.nivasafinance.features.lead.dto.WithdrawLeadRequest;
import com.nivasafinance.features.lead.dto.UpdateCallDetailsRequest;

import java.util.UUID;

public interface LeadWriteService {
    CreateLeadResponse createLead(CreateLeadRequest request);
    void updateLead(UUID leadIdentifier, UpdateLeadRequest request);
    // to update updatedby and updated at
    void touchLead(UUID leadIdentifier);
    void updatePreliminaryDetails(UUID leadIdentifier, UpdatePreliminaryDetailsRequest request);
    void updateCreditDetails(UUID leadIdentifier, UpdateCreditDetailsRequest request);
    void updateProposedDetails(UUID leadIdentifier, UpdateProposedDetailsRequest request);
    void updatePropertyDetails(UUID leadIdentifier, UpdatePropertyDetailsRequest request);
    void updateSourcingDetails(UUID leadIdentifier, UpdateSourcingDetailsRequest request);
    void updateCallDetails(UUID leadIdentifier, UpdateCallDetailsRequest request);
    void updateDisbursementDetails(UUID leadIdentifier, UpdateDisbursementDetailsRequest request);
    void createTranche(UUID leadIdentifier, CreateTrancheRequest request);
    void updateTranche(UUID leadIdentifier, UUID trancheIdentifier, UpdateTrancheRequest request);
    void deleteTranche(UUID leadIdentifier, UUID trancheIdentifier);
    void rejectLead(UUID leadIdentifier, RejectLeadRequest request);
    void undoRejectLead(UUID leadIdentifier);
    void withdrawLead(UUID leadIdentifier, WithdrawLeadRequest request);
    void completeLead(UUID leadIdentifier);
    void onholdLead(UUID leadIdentifier, OnholdLeadRequest request);
    void resumeLead(UUID leadIdentifier);
    void dropoffLead(UUID leadIdentifier, DropoffLeadRequest request);
}
