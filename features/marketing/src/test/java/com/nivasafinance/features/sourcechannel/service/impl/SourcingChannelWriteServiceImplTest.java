package com.nivasafinance.features.sourcechannel.service.impl;

import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.entity.SourcingChannel;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourcingChannelWriteServiceImplTest {

    @Mock
    private SourcingChannelRepositoryWrapper repositoryWrapper;
    @Mock
    private CodeValueMasterService codeValueMasterService;

    @InjectMocks
    private SourcingChannelWriteServiceImpl service;

    @Test
    void create_validatesFields_buildsEntityAndReturnsResponse() {
        SourcingChannelRequest request = new SourcingChannelRequest();
        request.setSourcingChannel("SRC");
        request.setMarketingSource("MSRC");
        request.setMarketingDetails(SourcingChannelRequest.MarketingDetails.builder()
                .sourceId("sid")
                .sourceUrl("url")
                .campaignId("cid")
                .referredByCode("ref")
                .build());

        SourcingChannel saved = new SourcingChannel();
        saved.setId(10L);
        saved.setSourcingIdentifier(UUID.randomUUID());

        when(repositoryWrapper.saveWithException(any(SourcingChannel.class))).thenReturn(saved);
        when(repositoryWrapper.findByIdAsResponseWithException(10L)).thenReturn(new SourcingChannelResponse());

        SourcingChannelResponse response = service.create(request);

        assertNotNull(response);
        verify(codeValueMasterService).getCodeValueByKeyAndCodeKey("SRC", SystemControlledMasterCodes.MARKETING_SOURCE_MASTER);
        verify(codeValueMasterService).getCodeValueByKeyAndCodeKey("MSRC", SystemControlledMasterCodes.MARKETING_CHANNEL_MASTER);
        ArgumentCaptor<SourcingChannel> captor = ArgumentCaptor.forClass(SourcingChannel.class);
        verify(repositoryWrapper).saveWithException(captor.capture());
        SourcingChannel toSave = captor.getValue();
        assertEquals("SRC", toSave.getSourcingChannel());
        assertEquals("MSRC", toSave.getMarketingSource());
        assertNotNull(toSave.getSourcingIdentifier());
        assertEquals("sid", toSave.getMarketingDetails().getSourceId());
        assertEquals("ref", toSave.getMarketingDetails().getReferredByCode());
    }

    @Test
    void update_mergesFieldsAndValidatesWhenPresent() {
        SourcingChannel existing = new SourcingChannel();
        existing.setId(5L);
        existing.setMarketingDetails(new SourcingChannel.MarketingDetails());

        SourcingChannelRequest request = new SourcingChannelRequest();
        request.setSourcingChannel("SRC");
        request.setMarketingSource("MSRC");
        request.setMarketingDetails(SourcingChannelRequest.MarketingDetails.builder()
                .campaignId("new-cid")
                .referredByCode("new-ref")
                .build());

        when(repositoryWrapper.findByIdWithException(5L)).thenReturn(existing);
        when(repositoryWrapper.saveWithException(any(SourcingChannel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repositoryWrapper.findByIdAsResponseWithException(5L)).thenReturn(new SourcingChannelResponse());

        SourcingChannelResponse response = service.update(5L, request);

        assertNotNull(response);
        verify(codeValueMasterService).getCodeValueByKeyAndCodeKey("SRC", SystemControlledMasterCodes.MARKETING_SOURCE_MASTER);
        verify(codeValueMasterService).getCodeValueByKeyAndCodeKey("MSRC", SystemControlledMasterCodes.MARKETING_CHANNEL_MASTER);
        ArgumentCaptor<SourcingChannel> captor = ArgumentCaptor.forClass(SourcingChannel.class);
        verify(repositoryWrapper).saveWithException(captor.capture());
        SourcingChannel saved = captor.getValue();
        assertEquals("SRC", saved.getSourcingChannel());
        assertEquals("MSRC", saved.getMarketingSource());
        assertEquals("new-cid", saved.getMarketingDetails().getCampaignId());
        assertEquals("new-ref", saved.getMarketingDetails().getReferredByCode());
    }

    @Test
    void update_skipsValidationWhenFieldsNull() {
        SourcingChannel existing = new SourcingChannel();
        existing.setId(6L);

        SourcingChannelRequest request = new SourcingChannelRequest(); // all null fields
        when(repositoryWrapper.findByIdWithException(6L)).thenReturn(existing);
        when(repositoryWrapper.saveWithException(existing)).thenReturn(existing);
        when(repositoryWrapper.findByIdAsResponseWithException(6L)).thenReturn(new SourcingChannelResponse());

        service.update(6L, request);

        verify(codeValueMasterService, never()).getCodeValueByKeyAndCodeKey(anyString(), any());
        verify(repositoryWrapper).saveWithException(existing);
    }
}
