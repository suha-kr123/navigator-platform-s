package com.nivasafinance.externals.whatsapp.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppStatusTrackerRequest {

    @NotBlank(message = "id is mandatory")
    private String id;

    @NotBlank(message = "timestamp is mandatory")
    private String timestamp;

    private String status;
    private String interaction;
    private JsonNode reply;
    private JsonNode click;
    private JsonNode errors;
}
