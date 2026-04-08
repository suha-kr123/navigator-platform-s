package com.nivasafinance.features.identifier.service.impl;

import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.common.validator.IdentifierValidator;
import com.nivasafinance.common.validator.IdentifierValidatorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentifierServiceImplTest {

    @Mock
    private IdentifierValidatorFactory identifierValidatorFactory;

    @Mock
    private IdentifierValidator identifierValidator;

    @InjectMocks
    private IdentifierServiceImpl identifierService;

    @Test
    void createIdentifierData_noValidator_skipsValidation() {
        IdentifierRequest request = new IdentifierRequest(IdentifierType.AADHAAR, "123456789012");
        when(identifierValidatorFactory.getValidator(IdentifierType.AADHAAR)).thenReturn(null);

        IdentifierData result = identifierService.createIdentifierData(request);

        assertNotNull(result.getId());
        assertEquals(IdentifierType.AADHAAR, result.getType());
        assertEquals("123456789012", result.getIdentifier());
        verify(identifierValidator, never()).validate(anyString());
    }

    @Test
    void createIdentifierData_withValidator_invokesValidate() {
        IdentifierRequest request = new IdentifierRequest(IdentifierType.PAN, "ABCDE1234F");
        when(identifierValidatorFactory.getValidator(IdentifierType.PAN)).thenReturn(identifierValidator);

        IdentifierData result = identifierService.createIdentifierData(request);

        assertNotNull(result.getId());
        assertEquals(IdentifierType.PAN, result.getType());
        assertEquals("ABCDE1234F", result.getIdentifier());
        verify(identifierValidator).validate("ABCDE1234F");
    }

    @Test
    void createIdentifierData_validatorThrows_propagates() {
        IdentifierRequest request = new IdentifierRequest(IdentifierType.PAN, "BAD");
        when(identifierValidatorFactory.getValidator(IdentifierType.PAN)).thenReturn(identifierValidator);
        doThrow(new IllegalArgumentException("invalid pan")).when(identifierValidator).validate("BAD");

        assertThrows(IllegalArgumentException.class, () -> identifierService.createIdentifierData(request));
        verify(identifierValidator).validate("BAD");
    }
}
