package com.nivasafinance.externals.customer.lead.service.impl;

import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.externals.customer.lead.service.LeadContactExternalService;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadContactExternalServiceImpl implements LeadContactExternalService {

    private final LeadContactReadService leadContactReadService;
    private final LeadContactWriteService leadContactWriteService;

    @Override
    @Transactional
    public IdentifierData addOrReplacePan(UUID leadId, UUID contactIdentifier, String pan) {
        List<IdentifierData> identifiers = leadContactReadService.getIdentifiers(leadId, contactIdentifier);
        Optional<IdentifierData> existingPan = identifiers.stream()
                .filter(id -> id.getType() == IdentifierType.PAN)
                .findFirst();

        IdentifierRequest request = new IdentifierRequest(IdentifierType.PAN, pan);

        if (existingPan.isPresent()) {
            leadContactWriteService.updateIdentifier(leadId, contactIdentifier, existingPan.get().getId(), request);
            return leadContactReadService.getIdentifier(leadId, contactIdentifier, existingPan.get().getId());
        }
        return leadContactWriteService.addIdentifier(leadId, contactIdentifier, request);
    }

    @Override
    public Optional<IdentifierData> getPan(UUID leadId, UUID contactIdentifier) {
        return leadContactReadService.getIdentifiers(leadId, contactIdentifier).stream()
                .filter(id -> id.getType() == IdentifierType.PAN)
                .findFirst();
    }
}
