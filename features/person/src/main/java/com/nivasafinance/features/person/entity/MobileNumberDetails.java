package com.nivasafinance.features.person.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.javers.core.metamodel.annotation.Value;

@Value
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileNumberDetails {
    private String number;
    private Boolean isPrimary;
    private Boolean isWhatsappAvailable;
}

