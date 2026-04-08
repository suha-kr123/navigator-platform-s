package com.nivasafinance.features.advisoractivity.service.impl;

import com.nivasafinance.features.advisoractivity.dto.CreateAdvisorActivityRequest;
import com.nivasafinance.features.advisoractivity.dto.CreateAdvisorActivityResponse;
import com.nivasafinance.features.advisoractivity.entity.AdvisorActivity;
import com.nivasafinance.features.advisoractivity.enums.ResourceAction;
import com.nivasafinance.features.advisoractivity.enums.ResourceEnum;
import com.nivasafinance.features.advisoractivity.repository.AdvisorActivityRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorActivityWriteServiceImplTest {

    @Mock
    private AdvisorActivityRepositoryWrapper advisorActivityRepositoryWrapper;

    @InjectMocks
    private AdvisorActivityWriteServiceImpl advisorActivityWriteService;

    @Test
    void createAdvisorActivity_persistsAndReturnsResponse() {
        CreateAdvisorActivityRequest request = new CreateAdvisorActivityRequest();
        request.setAdvisorId(1L);
        request.setDescription("d");
        request.setResource(ResourceEnum.ADVISOR);
        request.setAction(ResourceAction.CREATE);
        request.setCreatedBy("creator");

        AdvisorActivity saved = new AdvisorActivity();
        saved.setId(99L);
        saved.setIdentifier(java.util.UUID.randomUUID());
        when(advisorActivityRepositoryWrapper.saveWithException(any(AdvisorActivity.class))).thenReturn(saved);

        CreateAdvisorActivityResponse response = advisorActivityWriteService.createAdvisorActivity(request);

        assertNotNull(response);
        assertEquals(saved.getId(), response.getId());
        assertEquals(saved.getIdentifier(), response.getIdentifier());

        ArgumentCaptor<AdvisorActivity> cap = ArgumentCaptor.forClass(AdvisorActivity.class);
        verify(advisorActivityRepositoryWrapper).saveWithException(cap.capture());
        assertEquals("creator", cap.getValue().getCreatedBy());
    }

    @Test
    void createAdvisorActivity_createdByNull_doesNotOverrideCreatedByOnEntity() {
        CreateAdvisorActivityRequest request = new CreateAdvisorActivityRequest();
        request.setAdvisorId(1L);
        request.setDescription("d");
        request.setResource(ResourceEnum.ADVISOR);
        request.setAction(ResourceAction.CREATE);
        request.setCreatedBy(null);

        AdvisorActivity saved = new AdvisorActivity();
        saved.setId(1L);
        saved.setIdentifier(java.util.UUID.randomUUID());
        when(advisorActivityRepositoryWrapper.saveWithException(any(AdvisorActivity.class))).thenReturn(saved);

        advisorActivityWriteService.createAdvisorActivity(request);

        ArgumentCaptor<AdvisorActivity> cap = ArgumentCaptor.forClass(AdvisorActivity.class);
        verify(advisorActivityRepositoryWrapper).saveWithException(cap.capture());
        assertNull(cap.getValue().getCreatedBy());
    }
}
