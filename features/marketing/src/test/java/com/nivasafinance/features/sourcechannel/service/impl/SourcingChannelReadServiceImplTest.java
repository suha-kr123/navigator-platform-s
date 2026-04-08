package com.nivasafinance.features.sourcechannel.service.impl;

import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SourcingChannelReadServiceImplTest {

    @Mock
    private SourcingChannelRepositoryWrapper repositoryWrapper;

    @InjectMocks
    private SourcingChannelReadServiceImpl service;

    @Test
    void getById_delegatesToRepository() {
        SourcingChannelResponse response = new SourcingChannelResponse();
        when(repositoryWrapper.findByIdAsResponseWithException(5L)).thenReturn(response);

        SourcingChannelResponse result = service.getById(5L);

        assertEquals(response, result);
        verify(repositoryWrapper).findByIdAsResponseWithException(5L);
    }

    @Test
    void getBySourcingIdentifier_delegatesToRepository() {
        SourcingChannelResponse response = new SourcingChannelResponse();
        when(repositoryWrapper.findBySourcingIdentifierAsResponseWithException("src-1")).thenReturn(response);

        SourcingChannelResponse result = service.getBySourcingIdentifier("src-1");

        assertEquals(response, result);
        verify(repositoryWrapper).findBySourcingIdentifierAsResponseWithException("src-1");
    }
}
