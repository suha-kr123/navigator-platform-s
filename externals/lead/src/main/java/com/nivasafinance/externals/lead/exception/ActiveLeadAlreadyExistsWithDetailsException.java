package com.nivasafinance.externals.lead.exception;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.exception.ActiveLeadAlreadyExistsException;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.List;
import java.util.UUID;

public class ActiveLeadAlreadyExistsWithDetailsException extends ActiveLeadAlreadyExistsException {

    @Serial
    private static final long serialVersionUID = 3847561923847561924L;

    private final UUID leadIdentifier;
    private final transient List<AddressData> address;
    private final transient PreliminaryDetailsResponse preliminaryDetails;
    private final LeadStatus status;
    private final String stage;

    public ActiveLeadAlreadyExistsWithDetailsException(
            String phoneNo,
            UUID leadIdentifier,
            List<AddressData> address,
            PreliminaryDetailsResponse preliminaryDetails,
            LeadStatus status,
            String stage,
            MessageSource messageSource) {
        super(phoneNo, messageSource);
        this.leadIdentifier = leadIdentifier;
        this.address = address;
        this.preliminaryDetails = preliminaryDetails;
        this.status = status;
        this.stage = stage;
    }

    public UUID getLeadIdentifier() {
        return leadIdentifier;
    }

    public List<AddressData> getAddress() {
        return address;
    }

    public PreliminaryDetailsResponse getPreliminaryDetails() {
        return preliminaryDetails;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public String getStage() {
        return stage;
    }
}
