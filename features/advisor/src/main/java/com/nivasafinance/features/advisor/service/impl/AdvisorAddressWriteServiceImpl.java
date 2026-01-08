package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.AdvisorUpdateEventPayload;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorAddressWriteService;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AdvisorAddressWriteServiceImpl implements AdvisorAddressWriteService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final PersonWriteService personWriteService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PersonRepositoryWrapper personRepositoryWrapper;

    @Override
    public String addAddress(UUID advisorIdentifier, AddressRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        String addressId = personWriteService.addAddress(advisor.getPersonId(), request);
        
        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);
        
        return addressId;
    }

    @Override
    public AddressData updateAddress(UUID advisorIdentifier, String addressId, AddressRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        AddressData result = personWriteService.updateAddress(advisor.getPersonId(), addressId, request);

        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);

        return result;
    }

    private void publishAdvisorUpdatedEvent(Advisor advisor) {
        // Get primary mobile number from person entity
        com.nivasafinance.features.person.entity.Person person = 
                personRepositoryWrapper.findByIdWithException(advisor.getPersonId());
        String mobileNumber = null;
        if (person.getMobileNumbers() != null && !person.getMobileNumbers().isEmpty()) {
            mobileNumber = person.getMobileNumbers().stream()
                    .filter(m -> m.getIsPrimary() != null && m.getIsPrimary())
                    .map(com.nivasafinance.features.person.entity.MobileNumberDetails::getNumber)
                    .findFirst()
                    .orElse(null);
        }

        AdvisorUpdateEventPayload payload = AdvisorUpdateEventPayload.builder()
                .id(advisor.getId())
                .advisorIdentifier(advisor.getIdentifier())
                .mobileNumber(mobileNumber)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.ADVISOR_UPDATED.toString(), payload, username)
        );
    }

}
