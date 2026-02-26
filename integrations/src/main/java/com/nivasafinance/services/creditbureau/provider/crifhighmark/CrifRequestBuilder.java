package com.nivasafinance.services.creditbureau.provider.crifhighmark;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.services.creditbureau.dto.CreditBureauPersonData;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationClientException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class CrifRequestBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final String CONSENT = "Y";
    private static final String PAYMENT_FLAG = "N";
    private static final String ALERT_FLAG = "N";
    private static final String REPORT_FLAG = "Y";
    private static final String JSON_FLAG = "Y";
    private static final String REDIRECT_URL = "https://cir.crifhighmark.com/Inquiry/B2B/secureService.action";
    private static final String DEFAULT_EMAIL = "abc@abc.com";
    private static final String DEFAULT_PINCODE = "570001";
    private static final String DEFAULT_ADDRESS_LINE = "Bengaluru";
    private static final String DEFAULT_DOB = "01-01-1990";
    private static final String DEFAULT_AGE = "40";

    private String sanitizeField(String value) {
        return value == null || value.trim().isEmpty() ? "" : value;
    }

    public String buildStage1Payload(CreditBureauPersonData personData, String customerId, String productCode, List<AddressData> addresses, List<IdentifierData> identifiers) {
        if (personData == null) {
            throw new NavigatorIntegrationClientException("Person data cannot be null");
        }

        // Validate mandatory fields
        validateMandatoryFields(personData, addresses);

        List<String> fields = new ArrayList<>();

        // 1. firstName (NOT NULL)
        fields.add(sanitizeField(personData.getFirstName()));

        // 2. middleName (NULLABLE)
        fields.add(sanitizeField(personData.getMiddleName()));

        // 3. lastName (NOT NULL)
        fields.add(sanitizeField(personData.getLastName()));

        // 4. gender (NULLABLE)
        fields.add(sanitizeField(formatGender(personData.getGender())));

        // 5. dob (uses DEFAULT_DOB when not provided)
        String dob = formatDob(personData.getDateOfBirth());
        fields.add(StringUtils.hasText(dob) ? dob : DEFAULT_DOB);

        // 6. ageAsOnToday (uses DEFAULT_AGE when not provided)
        String age = calculateAge(personData.getDateOfBirth());
        fields.add(StringUtils.hasText(age) ? age : DEFAULT_AGE);

        // 7. maritalStatus (NULLABLE)
        fields.add("");

        // 8-10. mobile numbers (mobile1 NOT NULL, mobile2-3 NULLABLE)
        List<String> mobileNumbers = personData.getMobileNumbers() != null
                ? personData.getMobileNumbers()
                : new ArrayList<>();
        fields.add(!mobileNumbers.isEmpty() ? mobileNumbers.get(0) : "");
        fields.add(mobileNumbers.size() > 1 ? mobileNumbers.get(1) : "");
        fields.add(mobileNumbers.size() > 2 ? mobileNumbers.get(2) : "");

        // 11-12. email addresses (email1 NOT NULL, email2 NULLABLE)
        fields.add(DEFAULT_EMAIL);  // email1 - use default
        fields.add("");  // email2 - empty

        // 13-20. identifiers
        IdentifierMap identifierMap = mapIdentifiers(identifiers);
        fields.add(sanitizeField(identifierMap.pan));
        fields.add(sanitizeField(identifierMap.dl));
        fields.add(sanitizeField(identifierMap.voterId));
        fields.add(sanitizeField(identifierMap.passport));
        fields.add(sanitizeField(identifierMap.rationCard));
        fields.add(sanitizeField(identifierMap.uid));
        fields.add(sanitizeField(identifierMap.otherId1));
        fields.add(sanitizeField(identifierMap.otherId2));

        // 21-23. family names (NULLABLE)
        fields.add("");  // fatherName
        fields.add("");  // motherName
        fields.add("");  // spouseName

        // 24-29. first address (addressLine1, village1, city1, state1, pincode1, country1 NOT NULL)
        AddressData address1 = addresses != null && !addresses.isEmpty() ? addresses.get(0) : null;
        fields.add(sanitizeField(address1 != null && StringUtils.hasText(address1.getAddress()) ? address1.getAddress() : DEFAULT_ADDRESS_LINE));
        fields.add(sanitizeField(address1 != null ? address1.getVillageName() : ""));
        fields.add(sanitizeField(address1 != null && StringUtils.hasText(address1.getTaluka()) ? address1.getTaluka() : ""));
        fields.add(sanitizeField(address1 != null && StringUtils.hasText(address1.getState()) ? address1.getState() : ""));
        fields.add(sanitizeField(address1 != null && StringUtils.hasText(address1.getPincode()) ? address1.getPincode() : DEFAULT_PINCODE));
        fields.add(sanitizeField(address1 != null && StringUtils.hasText(address1.getCountry()) ? address1.getCountry() : "INDIA"));

        // 30-35. second address (all NULLABLE) - addressLine2, village2, city2, state2, pincode2, country2
        AddressData address2 = addresses != null && addresses.size() > 1 ? addresses.get(1) : null;
        fields.add(sanitizeField(address2 != null ? address2.getAddress() : ""));
        fields.add(sanitizeField(address2 != null ? address2.getVillageName() : ""));
        fields.add(sanitizeField(address2 != null ? address2.getTaluka() : ""));
        fields.add(sanitizeField(address2 != null ? address2.getState() : ""));
        fields.add(sanitizeField(address2 != null && StringUtils.hasText(address2.getPincode()) ? address2.getPincode() : ""));
        fields.add(sanitizeField(address2 != null ? address2.getCountry() : ""));

        // 36. customerId (NOT NULL)
        fields.add(sanitizeField(customerId));

        // 37. productCode (NOT NULL)
        fields.add(sanitizeField(productCode));

        // 38. consent (NOT NULL)
        fields.add(CONSENT);

        // 39. NREGA (NULLABLE)
        fields.add("");

        // 40. CKYC (NULLABLE)
        fields.add("");

        return String.join("|", fields);
    }

    public String buildStage2Payload(String orderId, String reportId, String accessCode, String userAnswer) {
        List<String> fields = new ArrayList<>();
        fields.add(sanitizeField(orderId));
        fields.add(sanitizeField(reportId));
        fields.add(sanitizeField(accessCode));
        fields.add(REDIRECT_URL);
        fields.add(PAYMENT_FLAG);
        fields.add(ALERT_FLAG);
        fields.add(REPORT_FLAG);
        fields.add(sanitizeField(userAnswer));
        return String.join("|", fields);
    }

    public String buildStage3Payload(String orderId, String reportId, String accessCode) {
        List<String> fields = new ArrayList<>();
        fields.add(sanitizeField(orderId));
        fields.add(sanitizeField(reportId));
        fields.add(sanitizeField(accessCode));
        fields.add(REDIRECT_URL);
        fields.add(PAYMENT_FLAG);
        fields.add(ALERT_FLAG);
        fields.add(REPORT_FLAG);
        fields.add(JSON_FLAG);
        return String.join("|", fields);
    }

    public String generateAccessCode(String userId, String customerId, String productCode, String password) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);
        String accessString = String.join("|",
                sanitizeField(userId),
                sanitizeField(customerId),
                sanitizeField(productCode),
                sanitizeField(password),
                timestamp
        );
        return Base64.getEncoder().encodeToString(accessString.getBytes());
    }

    private void validateMandatoryFields(CreditBureauPersonData personData, List<AddressData> addresses) {
        List<String> errors = new ArrayList<>();

        if (!StringUtils.hasText(personData.getFirstName())) {
            errors.add("firstName is required");
        }
        if (!StringUtils.hasText(personData.getLastName())) {
            errors.add("lastName is required");
        }
        if (personData.getMobileNumbers() == null || personData.getMobileNumbers().isEmpty()) {
            errors.add("At least one mobile number is required");
        }
        // Email validation removed - will use default email if not available
        // addressLine1 uses DEFAULT_ADDRESS_LINE when not provided
        // pincode1 uses DEFAULT_PINCODE when not provided
        // dob and age use DEFAULT_DOB and DEFAULT_AGE when not provided
        // city1 (taluka), state1, and country1 will use defaults if not provided

        if (!errors.isEmpty()) {
            throw new NavigatorIntegrationClientException("Missing mandatory fields: " + String.join(", ", errors));
        }
    }

    private String formatGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return "";
        }
        String genderUpper = gender.trim().toUpperCase();
        if (genderUpper.equals("MALE") || genderUpper.equals("M")) {
            return "M";
        } else if (genderUpper.equals("FEMALE") || genderUpper.equals("F")) {
            return "F";
        }
        return "";
    }

    private String formatDob(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return "";
        }
        return dateOfBirth.format(DATE_FORMATTER);
    }

    private String calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return "";
        }
        LocalDate now = LocalDate.now();
        int age = now.getYear() - dateOfBirth.getYear();
        if (now.getDayOfYear() < dateOfBirth.getDayOfYear()) {
            age--;
        }
        return String.valueOf(age);
    }



    private IdentifierMap mapIdentifiers(List<IdentifierData> identifiers) {
        IdentifierMap map = new IdentifierMap();
        if (identifiers == null || identifiers.isEmpty()) {
            return map;
        }

        List<String> otherIds = new ArrayList<>();
        for (IdentifierData identifier : identifiers) {
            if (identifier == null || identifier.getIdentifier() == null) {
                continue;
            }
            String value = sanitizeField(identifier.getIdentifier());
            if (value.isEmpty()) {
                continue;
            }

            IdentifierType type = identifier.getType();
            if (type == null) {
                otherIds.add(value);
                continue;
            }

            switch (type) {
                case PAN:
                    if (map.pan.isEmpty()) {
                        map.pan = value;
                    }
                    break;
                case DRIVING_LICENSE:
                    if (map.dl.isEmpty()) {
                        map.dl = value;
                    }
                    break;
                case VOTER_ID:
                    if (map.voterId.isEmpty()) {
                        map.voterId = value;
                    }
                    break;
                case AADHAAR:
                    if (map.uid.isEmpty()) {
                        map.uid = value;
                    }
                    break;
                default:
                    otherIds.add(value);
                    break;
            }
        }

        if (!otherIds.isEmpty()) {
            map.otherId1 = otherIds.get(0);
        }
        if (otherIds.size() > 1) {
            map.otherId2 = otherIds.get(1);
        }

        return map;
    }

    private static class IdentifierMap {
        String pan = "";
        String dl = "";
        String voterId = "";
        String passport = "";
        String rationCard = "";
        String uid = "";
        String otherId1 = "";
        String otherId2 = "";
    }
}