package com.nivasafinance.features.bre.repository;

import com.nivasafinance.features.bre.entity.BRELogs;
import com.nivasafinance.features.bre.exception.BREConfigNotFoundException;
import com.nivasafinance.features.bre.exception.BREConfigOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BRELogRepositoryWrapperTest {

    @Mock
    private BRELogRepository breLogRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private BRELogRepositoryWrapper wrapper;

    // ── saveWithException ──

    @Test
    void saveWithException_whenSaveSucceeds_returnsSavedEntity() {
        BRELogs entity = BRELogs.builder().breConfigId(1L).build();
        when(breLogRepository.save(entity)).thenReturn(entity);

        BRELogs result = wrapper.saveWithException(entity);

        assertEquals(1L, result.getBreConfigId(),
                "Should return the saved log entity with correct config ID");
        verify(breLogRepository).save(entity);
    }

    @Test
    void saveWithException_whenDataAccessExceptionThrown_throwsBREConfigOperationException() {
        BRELogs entity = BRELogs.builder().breConfigId(1L).build();
        when(breLogRepository.save(entity)).thenThrow(new DataIntegrityViolationException("DB error"));

        assertThrows(BREConfigOperationException.class,
                () -> wrapper.saveWithException(entity),
                "Should throw BREConfigOperationException when DataAccessException occurs during log save");
    }

    // ── findByIdWithException ──

    @Test
    void findByIdWithException_whenFound_returnsLog() {
        BRELogs log = BRELogs.builder().breConfigId(1L).build();
        when(breLogRepository.findById(100L)).thenReturn(Optional.of(log));

        BRELogs result = wrapper.findByIdWithException(100L);

        assertEquals(1L, result.getBreConfigId(),
                "Should return the log matching the requested ID");
        verify(breLogRepository).findById(100L);
    }

    @Test
    void findByIdWithException_whenNotFound_throwsBREConfigNotFoundException() {
        when(breLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BREConfigNotFoundException.class,
                () -> wrapper.findByIdWithException(999L),
                "Should throw BREConfigNotFoundException when no log found for the given ID");
    }

    @Test
    void findByIdWithException_whenDataAccessExceptionThrown_throwsBREConfigOperationException() {
        when(breLogRepository.findById(100L)).thenThrow(new DataIntegrityViolationException("DB error"));

        assertThrows(BREConfigOperationException.class,
                () -> wrapper.findByIdWithException(100L),
                "Should throw BREConfigOperationException when DataAccessException occurs during log retrieval");
    }
}
