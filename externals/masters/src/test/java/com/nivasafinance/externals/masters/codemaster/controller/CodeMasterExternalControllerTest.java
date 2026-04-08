package com.nivasafinance.externals.masters.codemaster.controller;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeMasterExternalControllerTest {

    @Mock
    private CodeMasterService codeMasterService;

    @InjectMocks
    private CodeMasterExternalController controller;

    @Test
    void getCodeValuesByCodeKey_success_returnsOkWithValues() {
        List<CodeValueResponse> values = List.of(new CodeValueResponse());
        when(codeMasterService.getAllCodeValuesByCodeKey("GENDER", true, null)).thenReturn(values);

        ResponseEntity<List<CodeValueResponse>> result = controller.getCodeValuesByCodeKey("GENDER", true, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getCodeValuesByCodeKey_withContext_passesContextToService() {
        List<CodeValueResponse> values = List.of();
        when(codeMasterService.getAllCodeValuesByCodeKey("STATUS", false, "advisor")).thenReturn(values);

        ResponseEntity<List<CodeValueResponse>> result = controller.getCodeValuesByCodeKey("STATUS", false, "advisor");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(codeMasterService).getAllCodeValuesByCodeKey("STATUS", false, "advisor");
    }

    @Test
    void getCodeValuesByCodeKey_serviceThrowsIllegalArgument_returnsBadRequest() {
        when(codeMasterService.getAllCodeValuesByCodeKey("INVALID", true, null))
                .thenThrow(new IllegalArgumentException("Invalid code key"));

        ResponseEntity<List<CodeValueResponse>> result = controller.getCodeValuesByCodeKey("INVALID", true, null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
    }
}
