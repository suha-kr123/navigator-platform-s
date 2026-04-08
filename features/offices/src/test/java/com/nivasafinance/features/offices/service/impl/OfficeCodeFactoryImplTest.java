package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfficeCodeFactoryImplTest {

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private OfficeCodeFactoryImpl officeCodeFactory;

    private Office buildOffice(Long id, String name, String key, String code, Long parentId) {
        Office o = new Office();
        o.setId(id);
        o.setName(name);
        o.setKey(key);
        o.setCode(code);
        o.setParentId(parentId);
        o.setIsActive(true);
        return o;
    }

    @Test
    void generateOfficeCode_parentIdIsNull_delegatesToRootCodeGeneration() {
        when(officeRepository.findByParentIdIsNullOrderByCodeDesc()).thenReturn(List.of());

        String result = officeCodeFactory.generateOfficeCode(null);

        assertEquals("001", result);
        verify(officeRepository).findByParentIdIsNullOrderByCodeDesc();
        verify(officeRepository, never()).findById(any());
    }

    @Test
    void generateOfficeCode_parentIdIsNotNull_delegatesToChildCodeGeneration() {
        Office parent = buildOffice(1L, "Parent", "parent-key", "001", null);
        when(officeRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(officeRepository.findByParentIdOrderByCodeDesc(1L)).thenReturn(List.of());

        String result = officeCodeFactory.generateOfficeCode(1L);

        assertEquals("001.001", result);
        verify(officeRepository, never()).findByParentIdIsNullOrderByCodeDesc();
    }

    @Test
    void generateRootOfficeCode_noExistingRoots_returns001() {
        when(officeRepository.findByParentIdIsNullOrderByCodeDesc()).thenReturn(List.of());

        String result = officeCodeFactory.generateOfficeCode(null);

        assertEquals("001", result);
    }

    @Test
    void generateRootOfficeCode_existingRoots_incrementsHighest() {
        Office root = buildOffice(1L, "Root", "root-key", "005", null);
        when(officeRepository.findByParentIdIsNullOrderByCodeDesc()).thenReturn(List.of(root));

        String result = officeCodeFactory.generateOfficeCode(null);

        assertEquals("006", result);
    }

    @Test
    void generateRootOfficeCode_unparsableCode_fallbacksTo001() {
        Office root = buildOffice(1L, "Root", "root-key", "abc", null);
        when(officeRepository.findByParentIdIsNullOrderByCodeDesc()).thenReturn(List.of(root));

        String result = officeCodeFactory.generateOfficeCode(null);

        assertEquals("001", result);
    }

    @Test
    void generateChildOfficeCode_parentNotFound_throwsOfficeNotFoundException() {
        when(officeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OfficeNotFoundException.class, () -> officeCodeFactory.generateOfficeCode(99L));
    }

    @Test
    void generateChildOfficeCode_noChildren_returnsParentDot001() {
        Office parent = buildOffice(1L, "Parent", "parent-key", "001", null);
        when(officeRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(officeRepository.findByParentIdOrderByCodeDesc(1L)).thenReturn(List.of());

        String result = officeCodeFactory.generateOfficeCode(1L);

        assertEquals("001.001", result);
    }

    @Test
    void generateChildOfficeCode_existingChildren_incrementsSequence() {
        Office parent = buildOffice(1L, "Parent", "parent-key", "001", null);
        Office lastChild = buildOffice(2L, "Child", "child-key", "001.003", 1L);
        when(officeRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(officeRepository.findByParentIdOrderByCodeDesc(1L)).thenReturn(List.of(lastChild));

        String result = officeCodeFactory.generateOfficeCode(1L);

        assertEquals("001.004", result);
    }

    @Test
    void generateChildOfficeCode_unparsableChildSequence_fallbacksTo001() {
        Office parent = buildOffice(1L, "Parent", "parent-key", "001", null);
        Office lastChild = buildOffice(2L, "Child", "child-key", "001.abc", 1L);
        when(officeRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(officeRepository.findByParentIdOrderByCodeDesc(1L)).thenReturn(List.of(lastChild));

        String result = officeCodeFactory.generateOfficeCode(1L);

        assertEquals("001.001", result);
    }

    @Test
    void generateChildOfficeCode_deeplyNested_generatesCorrectCode() {
        Office parent = buildOffice(3L, "GrandChild", "gc-key", "001.002.003", 2L);
        Office lastChild = buildOffice(4L, "GreatGC", "ggc-key", "001.002.003.005", 3L);
        when(officeRepository.findById(3L)).thenReturn(Optional.of(parent));
        when(officeRepository.findByParentIdOrderByCodeDesc(3L)).thenReturn(List.of(lastChild));

        String result = officeCodeFactory.generateOfficeCode(3L);

        assertEquals("001.002.003.006", result);
    }
}
