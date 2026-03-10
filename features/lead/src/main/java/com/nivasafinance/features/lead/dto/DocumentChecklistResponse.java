package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChecklistResponse {

    private String aKhata;
    private String bKhata;
    private String saleDeed;
    private String propertyTax;
}
