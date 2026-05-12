package com.nivasafinance.features.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhatsappLogFilters {
    List<String> messageType;
    List<String> sentBy;
    List<String> status;
}
