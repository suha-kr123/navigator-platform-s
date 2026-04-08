package com.nivasafinance.features.bre.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.exception.BREConfigNotFoundException;
import com.nivasafinance.features.bre.exception.BREConfigOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BREConfigRepositoryWrapperTest {

    @Mock
    private BREConfigRepository breConfigRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private BREConfigRepositoryWrapper wrapper;

    // ── saveWithException ──

    @Test
    void saveWithException_whenSaveSucceeds_returnsSavedEntity() {
        BREConfigs entity = BREConfigs.builder().uname("test-config").build();
        when(breConfigRepository.save(entity)).thenReturn(entity);

        BREConfigs result = wrapper.saveWithException(entity);

        assertEquals("test-config", result.getUname(),
                "Should return the saved entity with correct uname");
        verify(breConfigRepository).save(entity);
    }

    @Test
    void saveWithException_whenDataAccessExceptionThrown_throwsBREConfigOperationException() {
        BREConfigs entity = BREConfigs.builder().uname("test-config").build();
        when(breConfigRepository.save(entity)).thenThrow(new DataIntegrityViolationException("DB error"));

        assertThrows(BREConfigOperationException.class,
                () -> wrapper.saveWithException(entity),
                "Should throw BREConfigOperationException when DataAccessException occurs during save");
    }

    // ── findByUnameWithException ──

    @Test
    void findByUnameWithException_whenFound_returnsConfig() {
        BREConfigs config = BREConfigs.builder().uname("my-config").build();
        when(breConfigRepository.findByUname("my-config")).thenReturn(Optional.of(config));

        BREConfigs result = wrapper.findByUnameWithException("my-config");

        assertEquals("my-config", result.getUname(),
                "Should return the config matching the uname");
        verify(breConfigRepository).findByUname("my-config");
    }

    @Test
    void findByUnameWithException_whenNotFound_throwsBREConfigNotFoundException() {
        when(breConfigRepository.findByUname("missing")).thenReturn(Optional.empty());

        assertThrows(BREConfigNotFoundException.class,
                () -> wrapper.findByUnameWithException("missing"),
                "Should throw BREConfigNotFoundException when no config found for uname");
    }

    @Test
    void findByUnameWithException_whenDataAccessExceptionThrown_throwsBREConfigOperationException() {
        when(breConfigRepository.findByUname("error-uname"))
                .thenThrow(new DataIntegrityViolationException("DB error"));

        assertThrows(BREConfigOperationException.class,
                () -> wrapper.findByUnameWithException("error-uname"),
                "Should throw BREConfigOperationException when DataAccessException occurs during find");
    }

    // ── findAllWithException ──

    @Test
    void findAllWithException_whenSuccessful_returnsPaginatedResponse() {
        BREConfigs config = BREConfigs.builder().uname("cfg-1").build();
        Page<BREConfigs> page = new PageImpl<>(List.of(config));
        when(breConfigRepository.findAll(any(Pageable.class))).thenReturn(page);

        PaginationRequest request = new PaginationRequest();
        request.setOffset(0);
        request.setLimit(10);
        request.setSortBy("createdAt");
        request.setSortDirection("DESC");

        PaginatedResponse<BREConfigs> result = wrapper.findAllWithException(request);

        assertFalse(result.getContent().isEmpty(),
                "Should return non-empty content when configs exist");
        assertEquals("cfg-1", result.getContent().get(0).getUname(),
                "First item should match the expected config uname");
    }

    @Test
    void findAllWithException_whenSortByIsNull_usesDefaultSortBy() {
        Page<BREConfigs> page = new PageImpl<>(List.of());
        when(breConfigRepository.findAll(any(Pageable.class))).thenReturn(page);

        PaginationRequest request = new PaginationRequest();
        request.setOffset(0);
        request.setLimit(10);
        request.setSortBy(null);
        request.setSortDirection("ASC");

        PaginatedResponse<BREConfigs> result = wrapper.findAllWithException(request);

        assertNotNull(result, "Should return a paginated response even with null sortBy");
    }

    @Test
    void findAllWithException_whenSortDirectionIsNull_usesDefaultDirection() {
        Page<BREConfigs> page = new PageImpl<>(List.of());
        when(breConfigRepository.findAll(any(Pageable.class))).thenReturn(page);

        PaginationRequest request = new PaginationRequest();
        request.setOffset(0);
        request.setLimit(10);
        request.setSortBy("uname");
        request.setSortDirection(null);

        PaginatedResponse<BREConfigs> result = wrapper.findAllWithException(request);

        assertNotNull(result, "Should return a paginated response even with null sortDirection");
    }

    @Test
    void findAllWithException_whenDataAccessExceptionThrown_throwsBREConfigOperationException() {
        when(breConfigRepository.findAll(any(Pageable.class)))
                .thenThrow(new DataIntegrityViolationException("DB error"));

        PaginationRequest request = new PaginationRequest();
        request.setOffset(0);
        request.setLimit(10);

        assertThrows(BREConfigOperationException.class,
                () -> wrapper.findAllWithException(request),
                "Should throw BREConfigOperationException when DataAccessException occurs during findAll");
    }
}
