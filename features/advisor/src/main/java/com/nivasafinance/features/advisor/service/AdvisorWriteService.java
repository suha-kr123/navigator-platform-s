package com.nivasafinance.features.advisor.service;

import com.nivasafinance.features.advisor.dto.*;

import java.util.UUID;

public interface AdvisorWriteService {
    
    UUID createAdvisor(CreateAdvisorRequest request);
    
    void updateAdvisor(UUID identifier, UpdateAdvisorRequest request);
    
    void updateSourcingDetails(UUID identifier, UpdateSourcingDetailsRequest request);
    
    void updateQualificationDetails(UUID identifier, UpdateQualificationDetailsRequest request);
    
    void updateOccupationDetails(UUID identifier, UpdateOccupationDetailsRequest request);
    
    void updateSegmentationDetails(UUID identifier, UpdateSegmentationDetailsRequest request);
    
    
    void rejectAdvisor(UUID identifier, RejectAdvisorRequest request);
    
    void dormantAdvisor(UUID identifier, DormantAdvisorRequest request);
    
    void activateAdvisor(UUID identifier);
}

