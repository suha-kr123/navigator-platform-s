package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchDocumentChecklistRequest {

    private Optional<String> ekhataType;
    private Optional<String> ekhataStatus;
    private Optional<String> saleDeed;
    private Optional<String> propertyTax;
    private Optional<String> statementOfAccounts;
    private Optional<String> otherDocs;
}
