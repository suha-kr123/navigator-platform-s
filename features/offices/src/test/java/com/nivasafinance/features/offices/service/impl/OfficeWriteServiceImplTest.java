package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.offices.dto.OfficeCreateRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import com.nivasafinance.features.offices.repository.OfficeRepositoryWrapper;
import com.nivasafinance.features.offices.service.OfficeCodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfficeWriteServiceImplTest {

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;

    @Mock
    private AddressDataService addressDataService;

    @Mock
    private OfficeCodeFactory officeCodeFactory;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private OfficeWriteServiceImpl officeWriteService;

    @BeforeEach
    void stubMessageSource() {
        lenient().when(messageSource.getMessage(any(), any(), any())).thenReturn("err");
    }

    @Test
    void createOffice_duplicateKey_throwsBadRequest() {
        Office existing = new Office();
        existing.setKey("dup");
        when(officeRepository.findByKey("dup")).thenReturn(Optional.of(existing));

        OfficeCreateRequest req = new OfficeCreateRequest("N", "dup", null, null);

        assertThrows(BadRequestException.class, () -> officeWriteService.createOffice(req));
        verify(officeRepository, never()).save(any());
    }

    @Test
    void createOffice_withoutAddress_savesAndReturns() {
        when(officeRepository.findByKey("k1")).thenReturn(Optional.empty());
        when(officeCodeFactory.generateOfficeCode(null)).thenReturn("001");
        when(officeRepository.save(any(Office.class))).thenAnswer(inv -> {
            Office o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });

        OfficeCreateRequest req = new OfficeCreateRequest("Branch", "k1", null, null);
        OfficeResponse result = officeWriteService.createOffice(req);

        assertEquals(10L, result.getId());
        assertEquals("Branch", result.getName());
        assertEquals("k1", result.getKey());
        assertEquals("001", result.getCode());
        assertTrue(result.getIsActive());
        verify(addressDataService, never()).createAddressData(any());
    }

    @Test
    void createOffice_withAddress_embedsAddress() {
        AddressRequest ar = new AddressRequest();
        AddressData created = new AddressData();
        created.setAddress("Line 1");
        when(officeRepository.findByKey("k2")).thenReturn(Optional.empty());
        when(officeCodeFactory.generateOfficeCode(5L)).thenReturn("001.001");
        when(addressDataService.createAddressData(ar)).thenReturn(created);
        when(officeRepository.save(any(Office.class))).thenAnswer(inv -> {
            Office o = inv.getArgument(0);
            o.setId(11L);
            return o;
        });

        OfficeCreateRequest req = new OfficeCreateRequest("Sub", "k2", ar, 5L);
        OfficeResponse result = officeWriteService.createOffice(req);

        assertEquals("Line 1", result.getAddress().getAddress());
    }

    @Test
    void createOffice_dataIntegrityDuplicateKey_mapsToBadRequest() {
        when(officeRepository.findByKey("k3")).thenReturn(Optional.empty());
        when(officeCodeFactory.generateOfficeCode(null)).thenReturn("001");
        when(officeRepository.save(any(Office.class)))
                .thenThrow(new DataIntegrityViolationException("n_office_key_key duplicate"));

        OfficeCreateRequest req = new OfficeCreateRequest("X", "k3", null, null);

        assertThrows(BadRequestException.class, () -> officeWriteService.createOffice(req));
    }

    @Test
    void createOffice_dataIntegrityOther_rethrows() {
        when(officeRepository.findByKey("k4")).thenReturn(Optional.empty());
        when(officeCodeFactory.generateOfficeCode(null)).thenReturn("001");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("other constraint");
        when(officeRepository.save(any(Office.class))).thenThrow(ex);

        OfficeCreateRequest req = new OfficeCreateRequest("X", "k4", null, null);

        assertSame(ex, assertThrows(DataIntegrityViolationException.class,
                () -> officeWriteService.createOffice(req)));
    }

    @Test
    void moveOffice_officeNotFound_throws() {
        when(officeRepository.findByKey("missing")).thenReturn(Optional.empty());

        assertThrows(OfficeNotFoundException.class,
                () -> officeWriteService.moveOffice("missing", "parent"));
    }

    @Test
    void moveOffice_newParentNotFound_throws() {
        Office office = office("o1", 1L, "001", null);
        when(officeRepository.findByKey("o1")).thenReturn(Optional.of(office));
        when(officeRepository.findByKey("p-missing")).thenReturn(Optional.empty());

        assertThrows(OfficeNotFoundException.class,
                () -> officeWriteService.moveOffice("o1", "p-missing"));
    }

    @Test
    void moveOffice_underOwnDescendant_throwsIllegalArgument() {
        Office moving = office("o1", 1L, "001", null);
        Office newParent = office("child", 2L, "001.001", 1L);
        when(officeRepository.findByKey("o1")).thenReturn(Optional.of(moving));
        when(officeRepository.findByKey("child")).thenReturn(Optional.of(newParent));

        assertThrows(IllegalArgumentException.class,
                () -> officeWriteService.moveOffice("o1", "child"));
    }

    @Test
    void moveOffice_blankNewParent_movesToRoot() {
        Office moving = office("o1", 1L, "001", 5L);
        when(officeRepository.findByKey("o1")).thenReturn(Optional.of(moving));
        when(officeCodeFactory.generateOfficeCode(null)).thenReturn("NEW");
        when(officeRepositoryWrapper.findAllByCodePrefix("001")).thenReturn(List.of(moving));

        OfficeResponse result = officeWriteService.moveOffice("o1", "   ");

        assertEquals("NEW", result.getCode());
        assertNull(result.getParentId());
        verify(officeRepository, atLeastOnce()).save(moving);
    }

    @Test
    void moveOffice_withParent_updatesSelfAndDescendantCodes() {
        Office moving = office("o1", 1L, "001", null);
        Office newParent = office("p1", 2L, "002", null);
        Office child = office("c1", 3L, "001.001", 1L);
        when(officeRepository.findByKey("o1")).thenReturn(Optional.of(moving));
        when(officeRepository.findByKey("p1")).thenReturn(Optional.of(newParent));
        when(officeCodeFactory.generateOfficeCode(2L)).thenReturn("002.001");
        when(officeRepositoryWrapper.findAllByCodePrefix("001")).thenReturn(List.of(moving, child));

        officeWriteService.moveOffice("o1", "p1");

        assertEquals("002.001", moving.getCode());
        assertEquals(Long.valueOf(2L), moving.getParentId());
        assertEquals("002.001.001", child.getCode());
        verify(officeRepository, times(2)).save(any(Office.class));
    }

    @Test
    void moveOffice_skipsSelfInDescendantLoop() {
        Office moving = office("o1", 1L, "001", null);
        Office newParent = office("p1", 2L, "002", null);
        when(officeRepository.findByKey("o1")).thenReturn(Optional.of(moving));
        when(officeRepository.findByKey("p1")).thenReturn(Optional.of(newParent));
        when(officeCodeFactory.generateOfficeCode(2L)).thenReturn("002.001");
        when(officeRepositoryWrapper.findAllByCodePrefix("001")).thenReturn(List.of(moving));

        officeWriteService.moveOffice("o1", "p1");

        verify(officeRepository, times(1)).save(any(Office.class));
    }

    @Test
    void activateOffice_notFound_throws() {
        when(officeRepository.findByKey("x")).thenReturn(Optional.empty());

        assertThrows(OfficeNotFoundException.class, () -> officeWriteService.activateOffice("x"));
    }

    @Test
    void activateOffice_success() {
        Office o = office("k", 1L, "001", null);
        o.setIsActive(false);
        when(officeRepository.findByKey("k")).thenReturn(Optional.of(o));
        when(officeRepository.save(o)).thenReturn(o);

        OfficeResponse result = officeWriteService.activateOffice("k");

        assertTrue(result.getIsActive());
        verify(officeRepository).save(o);
    }

    @Test
    void deactivateOfficeCascade_notFound_throws() {
        when(officeRepository.findByKey("x")).thenReturn(Optional.empty());

        assertThrows(OfficeNotFoundException.class, () -> officeWriteService.deactivateOfficeCascade("x"));
    }

    @Test
    void deactivateOfficeCascade_deactivatesPrefixAndReturnsFresh() {
        Office root = office("root", 1L, "001", null);
        Office sub = office("sub", 2L, "001.001", 1L);
        when(officeRepository.findByKey("root")).thenReturn(Optional.of(root));
        when(officeRepositoryWrapper.findAllByCodePrefix("001")).thenReturn(List.of(root, sub));
        when(officeRepository.save(any(Office.class))).thenAnswer(inv -> inv.getArgument(0));

        OfficeResponse result = officeWriteService.deactivateOfficeCascade("root");

        assertFalse(result.getIsActive());
        assertFalse(root.getIsActive());
        assertFalse(sub.getIsActive());
        verify(officeRepository, times(2)).save(any(Office.class));
    }

    private static Office office(String key, Long id, String code, Long parentId) {
        Office o = new Office();
        o.setKey(key);
        o.setId(id);
        o.setCode(code);
        o.setParentId(parentId);
        o.setName("N");
        o.setIsActive(true);
        return o;
    }
}
