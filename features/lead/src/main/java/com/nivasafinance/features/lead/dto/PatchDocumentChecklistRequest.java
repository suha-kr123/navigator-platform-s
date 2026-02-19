package com.nivasafinance.features.lead.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("aKhata")
    private Optional<String> aKhata;
    @JsonProperty("bKhata")
    private Optional<String> bKhata;
    private Optional<String> saleDeed;
    private Optional<String> propertyTax;
}
