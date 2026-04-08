package com.nivasafinance.features.referral.service.impl;

import com.nivasafinance.features.referral.dto.ReferralCodeRegistryResponse;
import com.nivasafinance.features.referral.entity.ReferralCodeRegistry;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.referral.repository.ReferralCodeRegistryRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferralCodeRegistryServiceImplTest {

    @Mock
    private ReferralCodeRegistryRepositoryWrapper wrapper;

    @InjectMocks
    private ReferralCodeRegistryServiceImpl service;

    // ── generateReferralCode ────────────────────────────────────────
    @Test
    void generateReferralCode_whenExists_returnsExisting() {
        // Arrange
        UUID id = UUID.randomUUID();
        ReferralCodeRegistry existing = ReferralCodeRegistry.builder()
                .referredByCode("STA12345")
                .entityType(EntityType.STAFF)
                .entityIdentifier(id)
                .build();
        when(wrapper.findByEntity(EntityType.STAFF, id)).thenReturn(Optional.of(existing));
        // Act
        ReferralCodeRegistryResponse out = service.generateReferralCode(EntityType.STAFF, id);
        // Assert
        assertEquals("STA12345", out.getReferralCode(), "generateReferralCode should return existing code if present");
        verify(wrapper, never()).save(any());
    }

    @Test
    void generateReferralCode_whenNotExists_generatesUniqueAndSaves() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(wrapper.findByEntity(EntityType.APPLICANT, id)).thenReturn(Optional.empty());
        when(wrapper.existsByCode(anyString())).thenReturn(false);
        when(wrapper.save(any(ReferralCodeRegistry.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        ReferralCodeRegistryResponse out = service.generateReferralCode(EntityType.APPLICANT, id);
        // Assert
        assertNotNull(out.getReferralCode(), "generateReferralCode should return a generated code when not existing");
        assertTrue(out.getReferralCode().startsWith("APP"), "Generated code should have entity-specific prefix");
        verify(wrapper).save(any(ReferralCodeRegistry.class));
    }

    // ── getReferralCodeByCode ───────────────────────────────────────
    @Test
    void getReferralCodeByCode_whenNotFound_returnsNull() {
        // Arrange
        when(wrapper.findByCode("X")).thenReturn(Optional.empty());
        // Act
        ReferralCodeRegistryResponse out = service.getReferralCodeByCode("X");
        // Assert
        assertNull(out, "getReferralCodeByCode should return null when not found");
    }

    @Test
    void getReferralCodeByCode_whenFound_mapsToResponse() {
        // Arrange
        ReferralCodeRegistry e = ReferralCodeRegistry.builder()
                .referredByCode("ADV00001")
                .entityType(EntityType.ADVISOR)
                .entityIdentifier(UUID.randomUUID())
                .build();
        when(wrapper.findByCode("ADV00001")).thenReturn(Optional.of(e));
        // Act
        ReferralCodeRegistryResponse out = service.getReferralCodeByCode("ADV00001");
        // Assert
        assertEquals("ADV00001", out.getReferralCode(), "getReferralCodeByCode should map entity to response");
    }

    // ── getReferralCodeByEntity ─────────────────────────────────────
    @Test
    void getReferralCodeByEntity_whenMissing_returnsNull() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(wrapper.findByEntity(EntityType.STAFF, id)).thenReturn(Optional.empty());
        // Act
        ReferralCodeRegistryResponse out = service.getReferralCodeByEntity(EntityType.STAFF, id);
        // Assert
        assertNull(out, "getReferralCodeByEntity should return null when not found");
    }

    @Test
    void getReferralCodeByEntity_whenPresent_mapsToResponse() {
        // Arrange
        UUID id = UUID.randomUUID();
        ReferralCodeRegistry e = ReferralCodeRegistry.builder()
                .referredByCode("STA55555")
                .entityType(EntityType.STAFF)
                .entityIdentifier(id)
                .build();
        when(wrapper.findByEntity(EntityType.STAFF, id)).thenReturn(Optional.of(e));
        // Act
        ReferralCodeRegistryResponse out = service.getReferralCodeByEntity(EntityType.STAFF, id);
        // Assert
        assertEquals("STA55555", out.getReferralCode(), "getReferralCodeByEntity should map entity to response");
    }
}

