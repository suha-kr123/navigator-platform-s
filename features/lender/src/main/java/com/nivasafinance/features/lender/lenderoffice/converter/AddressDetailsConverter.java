package com.nivasafinance.features.lender.lenderoffice.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class AddressDetailsConverter implements AttributeConverter<LenderOffice.AddressDetails, String> {

    private static final Logger log = LoggerFactory.getLogger(AddressDetailsConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(LenderOffice.AddressDetails attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            log.error("Failed to serialize AddressDetails to JSON", e);
            return null;
        }
    }

    @Override
    public LenderOffice.AddressDetails convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        try {
            String jsonToParse = dbData;

            // Handle wrapped format: {"type":"jsonb","value":"...","null":false}
            if (dbData.contains("\"type\":\"jsonb\"") && dbData.contains("\"value\"")) {
                try {
                    JsonNode wrapper = objectMapper.readTree(dbData);
                    if (wrapper.has("value")) {
                        JsonNode valueNode = wrapper.get("value");
                        if (valueNode.isTextual()) {
                            jsonToParse = valueNode.asText();
                        } else {
                            jsonToParse = valueNode.toString();
                        }
                    }
                } catch (Exception e) {
                    // Fallback: extract string manually
                    int valueStart = dbData.indexOf("\"value\":\"") + 9;
                    int valueEnd = dbData.lastIndexOf("\"");
                    if (valueStart > 8 && valueEnd > valueStart) {
                        jsonToParse = dbData.substring(valueStart, valueEnd);
                        jsonToParse = jsonToParse.replace("\\\"", "\"").replace("\\\\", "\\");
                    }
                }
            }

            return objectMapper.readValue(jsonToParse, LenderOffice.AddressDetails.class);
        } catch (Exception e) {
            log.warn("Failed to deserialize AddressDetails from JSON: {}. Error: {}", dbData, e.getMessage());
            return null;
        }
    }
}

