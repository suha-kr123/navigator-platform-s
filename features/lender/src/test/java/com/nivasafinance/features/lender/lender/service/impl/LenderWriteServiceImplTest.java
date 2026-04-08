package com.nivasafinance.features.lender.lender.service.impl;

import com.nivasafinance.features.lender.lender.dto.LenderRequestData;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.dto.UpdateLenderRequest;
import com.nivasafinance.features.lender.lender.entity.Lender;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import com.nivasafinance.features.lender.lender.exception.LenderOperationException;
import com.nivasafinance.features.lender.lender.repository.LenderRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LenderWriteServiceImplTest {

    @Mock
    private LenderRepositoryWrapper lenderRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private LenderWriteServiceImpl lenderWriteService;

    @BeforeEach
    void stubMessageSource() {
        lenient().when(messageSource.getMessage(any(), any(), any())).thenReturn("err");
    }

    @Test
    void create_blankName_throws() {
        LenderRequestData data = new LenderRequestData();
        data.setName("  ");

        assertThrows(LenderOperationException.class, () -> lenderWriteService.create(data));
        verify(lenderRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void create_success_defaultActiveStatus() {
        LenderRequestData data = new LenderRequestData();
        data.setName("Acme Bank");
        data.setStatus(null);
        when(lenderRepositoryWrapper.findAll()).thenReturn(Collections.emptyList());
        when(lenderRepositoryWrapper.saveWithException(any(Lender.class))).thenAnswer(inv -> {
            Lender l = inv.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });

        LenderResponseData result = lenderWriteService.create(data);

        assertEquals("Acme Bank", result.getName());
        assertEquals(LenderStatus.ACTIVE, result.getStatus());
        verify(lenderRepositoryWrapper).saveWithException(any(Lender.class));
    }

    @Test
    void create_duplicateKeyBase_appendsCounter() {
        LenderRequestData data = new LenderRequestData();
        data.setName("Same Name");
        Lender existing = new Lender();
        existing.setKey("SAME_NAME");
        when(lenderRepositoryWrapper.findAll()).thenReturn(List.of(existing));
        when(lenderRepositoryWrapper.saveWithException(any(Lender.class))).thenAnswer(inv -> {
            Lender l = inv.getArgument(0);
            if (l.getId() == null) {
                l.setId(UUID.randomUUID());
            }
            return l;
        });

        LenderResponseData result = lenderWriteService.create(data);

        assertEquals("SAME_NAME_1", result.getKey());
    }

    @Test
    void create_nameNormalizingToEmpty_usesLenderBaseKey() {
        LenderRequestData data = new LenderRequestData();
        data.setName("@@@");
        when(lenderRepositoryWrapper.findAll()).thenReturn(Collections.emptyList());
        when(lenderRepositoryWrapper.saveWithException(any(Lender.class))).thenAnswer(inv -> {
            Lender l = inv.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });

        LenderResponseData result = lenderWriteService.create(data);

        assertEquals("LENDER", result.getKey());
    }

    @Test
    void update_success() {
        UUID id = UUID.randomUUID();
        Lender existing = new Lender();
        existing.setId(id);
        existing.setKey("K");
        existing.setName("Old");
        existing.setStatus(LenderStatus.ACTIVE);
        when(lenderRepositoryWrapper.findByIdWithException(id)).thenReturn(existing);
        when(lenderRepositoryWrapper.saveWithException(any(Lender.class))).thenAnswer(inv -> inv.getArgument(0));

        LenderResponseData result = lenderWriteService.update(id, new UpdateLenderRequest("New Name"));

        assertEquals("New Name", result.getName());
    }

    @Test
    void delete_delegatesToWrapper() {
        UUID id = UUID.randomUUID();
        lenderWriteService.delete(id);
        verify(lenderRepositoryWrapper).deleteByIdWithException(id);
    }

    @Test
    void activateDeactivate_fromActive_setsInactive() {
        UUID id = UUID.randomUUID();
        Lender l = new Lender();
        l.setId(id);
        l.setStatus(LenderStatus.ACTIVE);
        when(lenderRepositoryWrapper.findByIdWithException(id)).thenReturn(l);
        when(lenderRepositoryWrapper.saveWithException(any(Lender.class))).thenAnswer(inv -> inv.getArgument(0));

        LenderResponseData result = lenderWriteService.activateDeactivateLender(id);

        assertEquals(LenderStatus.INACTIVE, result.getStatus());
    }

    @Test
    void activateDeactivate_fromInactive_setsActive() {
        UUID id = UUID.randomUUID();
        Lender l = new Lender();
        l.setId(id);
        l.setStatus(LenderStatus.INACTIVE);
        when(lenderRepositoryWrapper.findByIdWithException(id)).thenReturn(l);
        when(lenderRepositoryWrapper.saveWithException(any(Lender.class))).thenAnswer(inv -> inv.getArgument(0));

        LenderResponseData result = lenderWriteService.activateDeactivateLender(id);

        assertEquals(LenderStatus.ACTIVE, result.getStatus());
    }

    @Test
    void toResponse_savedWithoutId_throws() {
        LenderRequestData data = new LenderRequestData();
        data.setName("X");
        when(lenderRepositoryWrapper.findAll()).thenReturn(Collections.emptyList());
        Lender broken = new Lender();
        broken.setId(null);
        broken.setKey("X");
        broken.setName("X");
        broken.setStatus(LenderStatus.ACTIVE);
        when(lenderRepositoryWrapper.saveWithException(any(Lender.class))).thenReturn(broken);

        assertThrows(IllegalStateException.class, () -> lenderWriteService.create(data));
    }
}
