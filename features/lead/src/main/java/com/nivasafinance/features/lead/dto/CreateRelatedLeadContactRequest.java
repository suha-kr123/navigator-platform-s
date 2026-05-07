package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.ContactRelationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRelatedLeadContactRequest {

    @NotNull
    @Valid
    private CreateLeadContactRequest contact;

    @NotNull
    private ContactRelationType relation;
}
