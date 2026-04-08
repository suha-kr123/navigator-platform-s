package com.nivasafinance.features.bre.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.bre.dto.BREConfigDetailedResponse;
import com.nivasafinance.features.bre.dto.BREConfigResponse;
import com.nivasafinance.features.bre.dto.BREConfigRuleFileUploadResponse;
import com.nivasafinance.features.bre.dto.CreateBREConfigRequest;
import com.nivasafinance.features.bre.enums.BREProvider;
import com.nivasafinance.features.bre.service.BREConfigReadService;
import com.nivasafinance.features.bre.service.BREConfigWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BREConfigControllerTest {

    @Mock
    private BREConfigReadService breConfigReadService;

    @Mock
    private BREConfigWriteService breConfigWriteService;

    @InjectMocks
    private BREConfigController controller;

    // ── createBREConfig ──

    @Test
    void createBREConfig_delegatesToWriteServiceAndReturnsCreated() {
        CreateBREConfigRequest request = CreateBREConfigRequest.builder()
                .uname("new-cfg")
                .provider(BREProvider.GORULES)
                .build();
        BREConfigDetailedResponse expected = BREConfigDetailedResponse.builder()
                .uname("new-cfg")
                .provider(BREProvider.GORULES)
                .build();
        when(breConfigWriteService.createBREConfig(request)).thenReturn(expected);

        ResponseEntity<BREConfigDetailedResponse> result = controller.createBREConfig(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode(),
                "Should return 201 CREATED when a new config is created");
        assertEquals("new-cfg", result.getBody().getUname(),
                "Response body should contain the created config uname");
        verify(breConfigWriteService).createBREConfig(request);
    }

    // ── getAllBREConfigs ──

    @Test
    void getAllBREConfigs_delegatesToReadServiceAndReturnsOk() {
        PaginationRequest paginationRequest = new PaginationRequest();
        BREConfigResponse configResponse = BREConfigResponse.builder()
                .uname("cfg-1")
                .provider(BREProvider.GORULES)
                .build();
        PaginationInfo paginationInfo = new PaginationInfo(0, 10, 1L, 1, 0, false, false);
        PaginatedResponse<BREConfigResponse> expected =
                new PaginatedResponse<>(List.of(configResponse), paginationInfo);
        when(breConfigReadService.getAllBREConfigs(paginationRequest)).thenReturn(expected);

        ResponseEntity<PaginatedResponse<BREConfigResponse>> result =
                controller.getAllBREConfigs(paginationRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode(),
                "Should return 200 OK when fetching all configs");
        assertEquals(1, result.getBody().getContent().size(),
                "Response should contain the expected number of configs");
    }

    // ── getBREConfigByUname ──

    @Test
    void getBREConfigByUname_delegatesToReadServiceAndReturnsOk() {
        BREConfigDetailedResponse expected = BREConfigDetailedResponse.builder()
                .uname("my-cfg")
                .build();
        when(breConfigReadService.getBREConfigByUname("my-cfg")).thenReturn(expected);

        ResponseEntity<BREConfigDetailedResponse> result =
                controller.getBREConfigByUname("my-cfg");

        assertEquals(HttpStatus.OK, result.getStatusCode(),
                "Should return 200 OK when fetching config by uname");
        assertEquals("my-cfg", result.getBody().getUname(),
                "Response body should contain the requested config");
    }

    // ── uploadRuleFile ──

    @Test
    void uploadRuleFile_delegatesToWriteServiceAndReturnsCreated() {
        MultipartFile file = mock(MultipartFile.class);
        UUID docIdentifier = UUID.randomUUID();
        BREConfigRuleFileUploadResponse expected = BREConfigRuleFileUploadResponse.builder()
                .ruleJsonFileId(42L)
                .documentIdentifier(docIdentifier)
                .build();
        when(breConfigWriteService.uploadRuleFile("my-cfg", file)).thenReturn(expected);

        ResponseEntity<BREConfigRuleFileUploadResponse> result =
                controller.uploadRuleFile("my-cfg", file);

        assertEquals(HttpStatus.CREATED, result.getStatusCode(),
                "Should return 201 CREATED when a rule file is uploaded");
        assertEquals(42L, result.getBody().getRuleJsonFileId(),
                "Response should contain the uploaded document ID");
        verify(breConfigWriteService).uploadRuleFile("my-cfg", file);
    }
}
