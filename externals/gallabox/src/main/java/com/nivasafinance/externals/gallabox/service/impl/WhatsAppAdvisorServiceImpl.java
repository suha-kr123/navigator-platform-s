package com.nivasafinance.externals.gallabox.service.impl;

import com.nivasafinance.externals.gallabox.dto.WhatsAppAdvisorRequest;
import com.nivasafinance.externals.gallabox.dto.WhatsAppAdvisorResponse;
import com.nivasafinance.externals.gallabox.service.WhatsAppAdvisorService;
import com.nivasafinance.features.advisor.dto.CreateAdvisorRequest;
import com.nivasafinance.features.advisor.dto.MobileNumberDetails;
import com.nivasafinance.features.advisor.dto.UpdateSegmentationDetailsRequest;
import com.nivasafinance.features.advisor.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class WhatsAppAdvisorServiceImpl implements WhatsAppAdvisorService {

    private final UserReadService userReadService;
    private final AdvisorReadService advisorReadService;
    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final AdvisorWriteService advisorWriteService;

    @Override
    @Transactional
    public WhatsAppAdvisorResponse createOrGetAdvisor(WhatsAppAdvisorRequest request) {
        // Map WhatsAppAdvisorRequest to CreateAdvisorRequest
        // Always set isWhatsapp to true for WhatsApp API advisors
        CreateAdvisorRequest createAdvisorRequest = new CreateAdvisorRequest();
        MobileNumberDetails mobileNumberDetails = new MobileNumberDetails(
                request.getMobileNumber(),
                true,  // isPrimary
                true   // isWhatsappAvailable - Always true for WhatsApp API
        );
        createAdvisorRequest.setMobileNumberDetails(mobileNumberDetails);

        Optional<Advisor> existingAdvisor = userReadService.findUserByPersonMobile(request.getMobileNumber())
                .flatMap(userResponse -> advisorReadService.findAdvisorByUsername(userResponse.getUsername()));

        UUID advisorIdentifier;

        if (existingAdvisor.isPresent()) {
            Advisor advisor = existingAdvisor.get();
            advisorIdentifier = advisor.getIdentifier();

            String name = toDisplayName(userReadService.getPersonForUser(advisor.getUsername()));
            String referralCode = advisor.getReferralCode();
            if (referralCode == null || referralCode.isBlank()) {
                referralCode = "empty";
            }
            String status = advisor.getStatus() != null ? advisor.getStatus().name() : null;

            return WhatsAppAdvisorResponse.builder()
                    .advisorIdentifier(advisorIdentifier)
                    .name(name)
                    .referralCode(referralCode)
                    .status(status)
                    .build();
        }

        // Advisor doesn't exist, delegate to features/advisor service to create it
        advisorIdentifier = advisorWriteService.createAdvisor(createAdvisorRequest);

        // Update segmentation_details to {"segmentation": "NEW"} for new advisor
        UpdateSegmentationDetailsRequest segmentationRequest = new UpdateSegmentationDetailsRequest();
        segmentationRequest.setSegmentation("NEW");
        advisorWriteService.updateSegmentationDetails(advisorIdentifier, segmentationRequest);

        // Update sourcing details if provided (only for new advisor creation)
        if (request.getSourcing_channel_name() != null) {
            updateSourcingDetails(advisorIdentifier, request);
        }

        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        String name = toDisplayName(userReadService.getPersonForUser(advisor.getUsername()));

        String referralCode = advisor.getReferralCode();
        if (referralCode == null || referralCode.isBlank()) {
            referralCode = "empty";
        }

        String status = advisor.getStatus() != null ? advisor.getStatus().name() : null;

        // Return response for newly created advisor
        return WhatsAppAdvisorResponse.builder()
                .advisorIdentifier(advisorIdentifier)
                .name(name)
                .referralCode(referralCode)
                .status(status)
                .build();
    }

    private void updateSourcingDetails(UUID advisorIdentifier, WhatsAppAdvisorRequest request) {
        UpdateSourcingDetailsRequest sourcingRequest = new UpdateSourcingDetailsRequest();
        // Always set sourcing_channel_name as DIRECT_WHATSAPP_SOURCE
        sourcingRequest.setSourcingChannel("DIRECT_WHATSAPP_SOURCE");

        if (request.getMarketing_details() != null) {
            boolean hasSourceId = request.getMarketing_details().getSourceId() != null && 
                                  !request.getMarketing_details().getSourceId().isBlank();
            boolean hasSourceUrl = request.getMarketing_details().getSourceUrl() != null && 
                                  !request.getMarketing_details().getSourceUrl().isBlank();
            
            // If at least one has a value, update both fields (even if one is empty)
            if (hasSourceId || hasSourceUrl) {
                sourcingRequest.setSourceId(request.getMarketing_details().getSourceId());
                sourcingRequest.setSourceUrl(request.getMarketing_details().getSourceUrl());
            }
            // If both are empty/null, don't update marketing_details (only sourcing_channel_name will be updated)

            // Set marketingSource from cf_marketing_source if not empty
            String cfMarketingSource = request.getMarketing_details().getCf_marketing_source();
            if (cfMarketingSource != null && !cfMarketingSource.isBlank()) {
                sourcingRequest.setMarketingSource(cfMarketingSource);
            }
        }

        advisorWriteService.updateSourcingDetails(advisorIdentifier, sourcingRequest);
    }

    private static String toDisplayName(PersonResponse person) {
        if (person == null) {
            return "empty";
        }
        String name = person.getDisplayName();
        return (name == null || name.isBlank()) ? "empty" : name;
    }
}
