package com.nivasafinance.features.lender.lenderoffice.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeRequestData;
import com.nivasafinance.features.lender.lenderoffice.dto.UpdateLenderOfficeRequest;
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.exception.LenderOfficeOperationException;
import com.nivasafinance.features.lender.lenderoffice.repository.LenderOfficeRepositoryWrapper;
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
class LenderOfficeWriteServiceImplTest {

    @Mock
    private AddressDataService addressDataService;

    @Mock
    private LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private LenderOfficeWriteServiceImpl lenderOfficeWriteService;

    @BeforeEach
    void stubMessageSource() {
        lenient().when(messageSource.getMessage(any(), any(), any())).thenReturn("err");
    }

    @Test
    void create_blankName_throws() {
        LenderOfficeRequestData data = new LenderOfficeRequestData();
        data.setName(" ");

        assertThrows(LenderOfficeOperationException.class,
                () -> lenderOfficeWriteService.create("L1", data));
        verify(lenderOfficeRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void create_withoutAddress_success() {
        LenderOfficeRequestData data = new LenderOfficeRequestData();
        data.setName("Branch A");
        data.setStatus(null);
        when(lenderOfficeRepositoryWrapper.findByLenderKey("L1")).thenReturn(Collections.emptyList());
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> {
            LenderOffice o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        LenderOfficeReponseData result = lenderOfficeWriteService.create("L1", data);

        assertEquals("BRANCH_A", result.getKey());
        assertEquals(LenderOfficeStatus.ACTIVE, result.getStatus());
        assertNull(result.getAddress());
        verify(addressDataService, never()).createAddressData(any());
    }

    @Test
    void create_withAddress_wrapsInDetails() {
        LenderOfficeRequestData data = new LenderOfficeRequestData();
        data.setName("HQ");
        AddressRequest ar = new AddressRequest();
        AddressData created = new AddressData();
        created.setAddress("St");
        when(lenderOfficeRepositoryWrapper.findByLenderKey("L1")).thenReturn(Collections.emptyList());
        when(addressDataService.createAddressData(ar)).thenReturn(created);
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> {
            LenderOffice o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });
        data.setCreateAddressRequest(ar);

        LenderOfficeReponseData result = lenderOfficeWriteService.create("L1", data);

        assertNotNull(result.getAddress());
        assertEquals("St", result.getAddress().getAddress());
    }

    @Test
    void create_duplicateKey_appendsCounter() {
        LenderOfficeRequestData data = new LenderOfficeRequestData();
        data.setName("Same");
        LenderOffice existing = new LenderOffice();
        existing.setKey("SAME");
        when(lenderOfficeRepositoryWrapper.findByLenderKey("L1")).thenReturn(List.of(existing));
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> {
            LenderOffice o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        LenderOfficeReponseData result = lenderOfficeWriteService.create("L1", data);

        assertEquals("SAME_1", result.getKey());
    }

    @Test
    void create_nameNormalizingToEmpty_usesOfficeBaseKey() {
        LenderOfficeRequestData data = new LenderOfficeRequestData();
        data.setName("###");
        when(lenderOfficeRepositoryWrapper.findByLenderKey("L1")).thenReturn(Collections.emptyList());
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> {
            LenderOffice o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        LenderOfficeReponseData result = lenderOfficeWriteService.create("L1", data);

        assertEquals("OFFICE", result.getKey());
    }

    @Test
    void update_clearsAddressWhenNoCreateRequest() {
        UUID id = UUID.randomUUID();
        LenderOffice existing = new LenderOffice();
        existing.setId(id);
        existing.setName("N");
        existing.setKey("K");
        existing.setLenderKey("L1");
        existing.setStatus(LenderOfficeStatus.ACTIVE);
        existing.setAddressDetails(LenderOffice.AddressDetails.builder().address(new AddressData()).build());
        LenderOfficeRequestData data = new LenderOfficeRequestData();
        data.setName("N2");
        data.setKey("K");
        data.setLenderKey("L1");
        data.setStatus(LenderOfficeStatus.ACTIVE);
        data.setCreateAddressRequest(null);
        when(lenderOfficeRepositoryWrapper.findByIdWithException(id)).thenReturn(existing);
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> inv.getArgument(0));

        LenderOfficeReponseData result = lenderOfficeWriteService.update(id, data);

        assertEquals("N2", result.getName());
        assertNull(result.getAddress());
    }

    @Test
    void update_withAddress_setsDetails() {
        UUID id = UUID.randomUUID();
        LenderOffice existing = new LenderOffice();
        existing.setId(id);
        existing.setName("N");
        existing.setKey("K");
        existing.setLenderKey("L1");
        existing.setStatus(LenderOfficeStatus.ACTIVE);
        AddressRequest ar = new AddressRequest();
        AddressData addr = new AddressData();
        addr.setPincode("560001");
        LenderOfficeRequestData data = new LenderOfficeRequestData();
        data.setName("N");
        data.setKey("K");
        data.setLenderKey("L1");
        data.setStatus(LenderOfficeStatus.ACTIVE);
        data.setCreateAddressRequest(ar);
        when(lenderOfficeRepositoryWrapper.findByIdWithException(id)).thenReturn(existing);
        when(addressDataService.createAddressData(ar)).thenReturn(addr);
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> inv.getArgument(0));

        LenderOfficeReponseData result = lenderOfficeWriteService.update(id, data);

        assertEquals("560001", result.getAddress().getPincode());
    }

    @Test
    void updateOffice_nullRequest_throws() {
        assertThrows(LenderOfficeOperationException.class,
                () -> lenderOfficeWriteService.updateOffice(UUID.randomUUID(), null));
    }

    @Test
    void updateOffice_trimsNameAndSetsAddress() {
        UUID id = UUID.randomUUID();
        LenderOffice existing = new LenderOffice();
        existing.setId(id);
        existing.setName("Old");
        existing.setKey("K");
        existing.setLenderKey("L1");
        existing.setStatus(LenderOfficeStatus.ACTIVE);
        AddressRequest ar = new AddressRequest();
        AddressData addr = new AddressData();
        addr.setAddress("A");
        UpdateLenderOfficeRequest req = new UpdateLenderOfficeRequest("  New Name  ", ar);
        when(lenderOfficeRepositoryWrapper.findByIdWithException(id)).thenReturn(existing);
        when(addressDataService.createAddressData(ar)).thenReturn(addr);
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> inv.getArgument(0));

        LenderOfficeReponseData result = lenderOfficeWriteService.updateOffice(id, req);

        assertEquals("New Name", result.getName());
        assertEquals("A", result.getAddress().getAddress());
    }

    @Test
    void updateOffice_blankName_skipsNameUpdate() {
        UUID id = UUID.randomUUID();
        LenderOffice existing = new LenderOffice();
        existing.setId(id);
        existing.setName("Keep");
        existing.setKey("K");
        existing.setLenderKey("L1");
        existing.setStatus(LenderOfficeStatus.ACTIVE);
        UpdateLenderOfficeRequest req = new UpdateLenderOfficeRequest("   ", null);
        when(lenderOfficeRepositoryWrapper.findByIdWithException(id)).thenReturn(existing);
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> inv.getArgument(0));

        LenderOfficeReponseData result = lenderOfficeWriteService.updateOffice(id, req);

        assertEquals("Keep", result.getName());
    }

    @Test
    void delete_delegates() {
        UUID id = UUID.randomUUID();
        lenderOfficeWriteService.delete(id);
        verify(lenderOfficeRepositoryWrapper).deleteByIdWithException(id);
    }

    @Test
    void activateDeactivate_fromActive_toInactive() {
        UUID id = UUID.randomUUID();
        LenderOffice o = new LenderOffice();
        o.setId(id);
        o.setStatus(LenderOfficeStatus.ACTIVE);
        when(lenderOfficeRepositoryWrapper.findByIdWithException(id)).thenReturn(o);
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(LenderOfficeStatus.INACTIVE, lenderOfficeWriteService.activateDeactivateLenderOffice(id).getStatus());
    }

    @Test
    void activateDeactivate_fromInactive_toActive() {
        UUID id = UUID.randomUUID();
        LenderOffice o = new LenderOffice();
        o.setId(id);
        o.setStatus(LenderOfficeStatus.INACTIVE);
        when(lenderOfficeRepositoryWrapper.findByIdWithException(id)).thenReturn(o);
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(LenderOfficeStatus.ACTIVE, lenderOfficeWriteService.activateDeactivateLenderOffice(id).getStatus());
    }

    @Test
    void toResponse_nullIdAfterSave_throws() {
        LenderOfficeRequestData data = new LenderOfficeRequestData();
        data.setName("X");
        when(lenderOfficeRepositoryWrapper.findByLenderKey("L1")).thenReturn(Collections.emptyList());
        LenderOffice broken = new LenderOffice();
        broken.setId(null);
        broken.setKey("X");
        broken.setName("X");
        broken.setLenderKey("L1");
        broken.setStatus(LenderOfficeStatus.ACTIVE);
        when(lenderOfficeRepositoryWrapper.saveWithException(any(LenderOffice.class))).thenReturn(broken);

        assertThrows(IllegalStateException.class, () -> lenderOfficeWriteService.create("L1", data));
    }
}
