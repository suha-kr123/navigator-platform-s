package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.ValidationException;

import java.io.Serial;

public class LeadContactValidationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LeadContactValidationException(String message) {
        super(message);
    }

    public static LeadContactValidationException duplicateAddressType(String addressType) {
        return new LeadContactValidationException(
                "An address of type " + addressType + " already exists for this contact. " +
                "Only one address per address type is allowed.");
    }

    public static LeadContactValidationException duplicateIdentifierType(String identifierType) {
        return new LeadContactValidationException(
                "An identifier of type " + identifierType + " already exists for this contact. " +
                "Only one identifier per identifier type is allowed.");
    }

    public static LeadContactValidationException invalidContactIdentifier(String identifier) {
        return new LeadContactValidationException(
                "Invalid contact identifier format: " + identifier + 
                ". Must be a valid UUID or a create reference (e.g., 'create:0')");
    }

    public static LeadContactValidationException invalidCreateReference(String reference, int index) {
        return new LeadContactValidationException(
                "Invalid create reference: " + reference + 
                ". Contact at index " + index + " was not created in this request.");
    }

    public static LeadContactValidationException invalidCreateReferenceFormat(String reference) {
        return new LeadContactValidationException(
                "Invalid create reference format: " + reference + 
                ". Expected format: 'create:0', 'create:1', etc.");
    }

    public static LeadContactValidationException missingRequiredField(String fieldName, String operation) {
        return new LeadContactValidationException(
                fieldName + " is required for " + operation + " operation");
    }

    public static LeadContactValidationException invalidOperation(String operation, String validOperations) {
        return new LeadContactValidationException(
                "Invalid operation: " + operation + ". Valid operations are: " + validOperations);
    }

    public static LeadContactValidationException duplicateContactPerson() {
        return new LeadContactValidationException(
                "This lead already has a contact linked to this person.");
    }

    public static LeadContactValidationException duplicatePhoneNumber() {
        return new LeadContactValidationException(
                "error.lead.contact.duplicate.phone.number");
    }
}

