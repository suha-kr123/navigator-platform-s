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
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
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
    private final UserWriteService userWriteService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserReadService userReadService;

    @Override
    public String addAddress(UUID advisorIdentifier, AddressRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        String addressId = userWriteService.addAddressForUser(advisor.getUsername(), request);

        publishAdvisorUpdatedEvent(advisor);

        return addressId;
    }

    @Override
    public AddressData updateAddress(UUID advisorIdentifier, String addressId, AddressRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        AddressData result = userWriteService.updateAddressForUser(advisor.getUsername(), addressId, request);

        publishAdvisorUpdatedEvent(advisor);

        return result;
    }

    private void publishAdvisorUpdatedEvent(Advisor advisor) {
        PersonResponse person = userReadService.getPersonForUser(advisor.getUsername());
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
