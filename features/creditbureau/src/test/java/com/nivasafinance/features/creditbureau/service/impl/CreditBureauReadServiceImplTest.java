package com.nivasafinance.features.creditbureau.service.impl;

import com.nivasafinance.features.creditbureau.dto.CustomerEnquiryResponse;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import com.nivasafinance.features.creditbureau.dto.DemographicVariationResponse;
import com.nivasafinance.features.creditbureau.dto.EnquiryStatusResponse;
import com.nivasafinance.features.creditbureau.dto.SummaryResponse;
import com.nivasafinance.features.creditbureau.dto.TrendsResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauCustomerEnquiry;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.entity.CreditBureauSummary;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import com.nivasafinance.features.creditbureau.repository.CreditBureauCustomerEnquiryRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauDemographicVariationRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauRepository;
import com.nivasafinance.features.creditbureau.repository.CreditBureauRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauSummaryRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauTrendsRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditBureauReadServiceImplTest {

    @Mock
    private CreditBureauRepositoryWrapper creditBureauRepositoryWrapper;

    @Mock
    private CreditBureauRepository creditBureauRepository;

    @Mock
    private CreditBureauCustomerEnquiryRepositoryWrapper creditBureauCustomerEnquiryRepositoryWrapper;

    @Mock
    private CreditBureauSummaryRepositoryWrapper creditBureauSummaryRepositoryWrapper;

    @Mock
    private CreditBureauTrendsRepositoryWrapper creditBureauTrendsRepositoryWrapper;

    @Mock
    private CreditBureauDemographicVariationRepositoryWrapper creditBureauDemographicVariationRepositoryWrapper;

    @InjectMocks
    private CreditBureauReadServiceImpl creditBureauReadService;

    private Long enquiryId;
    private UUID enquiryIdentifier;
    private CreditBureauEnquiry enquiry;

    @BeforeEach
    void setUp() {
        enquiryId = 1L;
        enquiryIdentifier = UUID.randomUUID();

        enquiry = new CreditBureauEnquiry();
        enquiry.setId(enquiryId);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.SUCCESS);
        enquiry.setProvider("CRIF_HIGHMARK");
        enquiry.setReportId("RPT-001");
    }

    // ==================== getCbEnquiryById() Tests ====================

    @Test
    void getCbEnquiryById_whenEnquiryExists_returnsResponse() {
        // Arrange
        when(creditBureauRepository.findById(enquiryId)).thenReturn(Optional.of(enquiry));

        // Act
        Optional<CreditBureauEnquiryResponse> result = creditBureauReadService.getCbEnquiryById(enquiryId);

        // Assert
        assertTrue(result.isPresent(), "Should return present Optional when enquiry exists");
        assertEquals(enquiryId, result.get().getId(), "Response ID should match enquiry ID");
        assertEquals(enquiryIdentifier, result.get().getIdentifier(), "Response identifier should match");
        assertEquals(CreditBureauEnquiryStatus.SUCCESS, result.get().getStatus(), "Response status should match");
        verify(creditBureauRepository).findById(enquiryId);
    }

    @Test
    void getCbEnquiryById_whenEnquiryNotFound_returnsEmptyOptional() {
        // Arrange
        when(creditBureauRepository.findById(enquiryId)).thenReturn(Optional.empty());

        // Act
        Optional<CreditBureauEnquiryResponse> result = creditBureauReadService.getCbEnquiryById(enquiryId);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty Optional when enquiry does not exist");
        verify(creditBureauRepository).findById(enquiryId);
    }

    // ==================== getCbEnquiryEntityById() Tests ====================

    @Test
    void getCbEnquiryEntityById_whenEnquiryExists_returnsEntity() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdWithException(enquiryId)).thenReturn(enquiry);

        // Act
        CreditBureauEnquiry result = creditBureauReadService.getCbEnquiryEntityById(enquiryId);

        // Assert
        assertNotNull(result, "Should return entity when enquiry exists");
        assertEquals(enquiryId, result.getId(), "Entity ID should match");
        verify(creditBureauRepositoryWrapper).findByIdWithException(enquiryId);
    }

    // ==================== getCbEnquiryEntityByIdentifier() Tests ====================

    @Test
    void getCbEnquiryEntityByIdentifier_whenEnquiryExists_returnsEntity() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifierWithException(enquiryIdentifier)).thenReturn(enquiry);

        // Act
        CreditBureauEnquiry result = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);

        // Assert
        assertNotNull(result, "Should return entity when enquiry exists");
        assertEquals(enquiryIdentifier, result.getIdentifier(), "Entity identifier should match");
        verify(creditBureauRepositoryWrapper).findByIdentifierWithException(enquiryIdentifier);
    }

    // ==================== getCustomerEnquiryByEnquiryIdentifier() Tests ====================

    @Test
    void getCustomerEnquiryByEnquiryIdentifier_whenEnquiryExists_returnsCustomerEnquiries() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.of(enquiry));

        CreditBureauCustomerEnquiry customerEnquiry = CreditBureauCustomerEnquiry.builder()
                .enquiryId(enquiryId)
                .lenderName("Test Lender")
                .inquiryDate(LocalDate.of(2025, 1, 15))
                .inquiryAmount(BigDecimal.valueOf(50000))
                .build();
        when(creditBureauCustomerEnquiryRepositoryWrapper.findByEnquiryId(enquiryId))
                .thenReturn(List.of(customerEnquiry));

        // Act
        List<CustomerEnquiryResponse> result = creditBureauReadService
                .getCustomerEnquiryByEnquiryIdentifier(enquiryIdentifier);

        // Assert
        assertEquals(1, result.size(), "Should return one customer enquiry");
        assertEquals("Test Lender", result.get(0).getLenderName(), "Lender name should be mapped");
        verify(creditBureauRepositoryWrapper).findByIdentifier(enquiryIdentifier);
        verify(creditBureauCustomerEnquiryRepositoryWrapper).findByEnquiryId(enquiryId);
    }

    @Test
    void getCustomerEnquiryByEnquiryIdentifier_whenEnquiryNotFound_throwsRuntimeException() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> creditBureauReadService.getCustomerEnquiryByEnquiryIdentifier(enquiryIdentifier),
                "Should throw RuntimeException when enquiry not found");
        verifyNoInteractions(creditBureauCustomerEnquiryRepositoryWrapper);
    }

    // ==================== getSummaryByEnquiryIdentifier() Tests ====================

    @Test
    void getSummaryByEnquiryIdentifier_whenSummaryExists_returnsSummary() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.of(enquiry));

        CreditBureauSummary summary = CreditBureauSummary.builder()
                .enquiryId(enquiryId)
                .creditScore(750)
                .totalAccounts(5)
                .build();
        when(creditBureauSummaryRepositoryWrapper.findByEnquiryId(enquiryId)).thenReturn(Optional.of(summary));

        // Act
        Optional<SummaryResponse> result = creditBureauReadService
                .getSummaryByEnquiryIdentifier(enquiryIdentifier);

        // Assert
        assertTrue(result.isPresent(), "Should return present Optional when summary exists");
        assertEquals(750, result.get().getCreditScore(), "Credit score should be mapped");
        verify(creditBureauSummaryRepositoryWrapper).findByEnquiryId(enquiryId);
    }

    @Test
    void getSummaryByEnquiryIdentifier_whenSummaryNotFound_returnsEmptyOptional() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.of(enquiry));
        when(creditBureauSummaryRepositoryWrapper.findByEnquiryId(enquiryId)).thenReturn(Optional.empty());

        // Act
        Optional<SummaryResponse> result = creditBureauReadService
                .getSummaryByEnquiryIdentifier(enquiryIdentifier);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty Optional when no summary exists");
    }

    @Test
    void getSummaryByEnquiryIdentifier_whenEnquiryNotFound_throwsRuntimeException() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> creditBureauReadService.getSummaryByEnquiryIdentifier(enquiryIdentifier),
                "Should throw RuntimeException when enquiry not found");
        verifyNoInteractions(creditBureauSummaryRepositoryWrapper);
    }

    // ==================== getEnquiryStatusByEnquiryIdentifier() Tests ====================

    @Test
    void getEnquiryStatusByEnquiryIdentifier_whenEnquiryExists_returnsStatus() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.of(enquiry));

        // Act
        Optional<EnquiryStatusResponse> result = creditBureauReadService
                .getEnquiryStatusByEnquiryIdentifier(enquiryIdentifier);

        // Assert
        assertTrue(result.isPresent(), "Should return present Optional when enquiry exists");
        assertEquals(enquiryIdentifier, result.get().getIdentifier(), "Identifier should match");
        assertEquals(CreditBureauEnquiryStatus.SUCCESS, result.get().getStatus(), "Status should match");
    }

    @Test
    void getEnquiryStatusByEnquiryIdentifier_whenEnquiryNotFound_throwsRuntimeException() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> creditBureauReadService.getEnquiryStatusByEnquiryIdentifier(enquiryIdentifier),
                "Should throw RuntimeException when enquiry not found");
    }

    // ==================== getTrendsByEnquiryIdentifier() Tests ====================

    @Test
    void getTrendsByEnquiryIdentifier_whenTrendsExist_returnsTrendsList() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.of(enquiry));

        TrendsResponse trend = TrendsResponse.builder()
                .date(LocalDate.of(2025, 1, 1))
                .scoreValue(720)
                .build();
        when(creditBureauTrendsRepositoryWrapper.findByEnquiryIdOrderByDateDesc(enquiryId))
                .thenReturn(List.of(trend));

        // Act
        List<TrendsResponse> result = creditBureauReadService
                .getTrendsByEnquiryIdentifier(enquiryIdentifier);

        // Assert
        assertEquals(1, result.size(), "Should return one trend entry");
        assertEquals(720, result.get(0).getScoreValue(), "Score value should match");
        verify(creditBureauTrendsRepositoryWrapper).findByEnquiryIdOrderByDateDesc(enquiryId);
    }

    @Test
    void getTrendsByEnquiryIdentifier_whenEnquiryNotFound_throwsRuntimeException() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> creditBureauReadService.getTrendsByEnquiryIdentifier(enquiryIdentifier),
                "Should throw RuntimeException when enquiry not found");
        verifyNoInteractions(creditBureauTrendsRepositoryWrapper);
    }

    // ==================== getDemographicVariationsByEnquiryIdentifier() Tests ====================

    @Test
    void getDemographicVariationsByEnquiryIdentifier_whenVariationsExist_returnsList() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.of(enquiry));

        DemographicVariationResponse variation = DemographicVariationResponse.builder()
                .variationType("ADDRESS")
                .variationValue("123 Main St")
                .build();
        when(creditBureauDemographicVariationRepositoryWrapper.findByEnquiryIdAsResponse(enquiryId))
                .thenReturn(List.of(variation));

        // Act
        List<DemographicVariationResponse> result = creditBureauReadService
                .getDemographicVariationsByEnquiryIdentifier(enquiryIdentifier);

        // Assert
        assertEquals(1, result.size(), "Should return one variation");
        assertEquals("ADDRESS", result.get(0).getVariationType(), "Variation type should match");
        verify(creditBureauDemographicVariationRepositoryWrapper).findByEnquiryIdAsResponse(enquiryId);
    }

    @Test
    void getDemographicVariationsByEnquiryIdentifier_whenEnquiryNotFound_throwsRuntimeException() {
        // Arrange
        when(creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> creditBureauReadService.getDemographicVariationsByEnquiryIdentifier(enquiryIdentifier),
                "Should throw RuntimeException when enquiry not found");
        verifyNoInteractions(creditBureauDemographicVariationRepositoryWrapper);
    }

    // ==================== getDemographicVariationsByEnquiryId() Tests ====================

    @Test
    void getDemographicVariationsByEnquiryId_delegatesToWrapper() {
        // Arrange
        DemographicVariationResponse variation = DemographicVariationResponse.builder()
                .variationType("PHONE")
                .variationValue("9876543210")
                .build();
        when(creditBureauDemographicVariationRepositoryWrapper.findByEnquiryIdAsResponse(enquiryId))
                .thenReturn(List.of(variation));

        // Act
        List<DemographicVariationResponse> result = creditBureauReadService
                .getDemographicVariationsByEnquiryId(enquiryId);

        // Assert
        assertEquals(1, result.size(), "Should return variations from wrapper");
        assertEquals("PHONE", result.get(0).getVariationType(), "Variation type should match");
        verify(creditBureauDemographicVariationRepositoryWrapper).findByEnquiryIdAsResponse(enquiryId);
    }
}
