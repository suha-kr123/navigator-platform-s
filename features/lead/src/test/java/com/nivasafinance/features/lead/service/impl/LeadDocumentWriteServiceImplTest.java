package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.GeoData;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.document.dto.DocumentCreateRequest;
import com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.dto.DocumentResponse;
import com.nivasafinance.features.document.service.DocumentReadService;
import com.nivasafinance.features.document.service.DocumentWriteService;
import com.nivasafinance.features.lead.dto.HouseFrontPhotoRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateResponse;
import com.nivasafinance.features.lead.dto.LeadDocumentUpdateRequest;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadDocumentWriteServiceImplTest {

    @Mock
    private DocumentWriteService documentWriteService;

    @Mock
    private DocumentReadService documentReadService;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private CodeValueMasterService codeValueMasterService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private LeadDocumentWriteServiceImpl leadDocumentWriteService;

    private UUID leadIdentifier;
    private UUID documentIdentifier;
    private Long leadId;
    private Long documentId;
    private Lead lead;

    @BeforeEach
    void setUp() {
        UserContext.setUsername("test-user");

        leadIdentifier = UUID.randomUUID();
        documentIdentifier = UUID.randomUUID();
        leadId = 1L;
        documentId = 100L;

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);
        lead.setDocumentDetails(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        UserContext.setUsername(null);
    }

    // ==================== createLeadDocument(MultipartFile) Tests ====================

    @Test
    void createLeadDocument_withMultipartFile_createsAndReturnsResponse() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());
        LeadDocumentCreateRequest request = LeadDocumentCreateRequest.builder()
                .name("doc.pdf").tags(List.of("TAG_1")).build();
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder()
                .id(documentId).identifier(documentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadDocumentCreateResponse result = leadDocumentWriteService.createLeadDocument(leadIdentifier, file, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(documentIdentifier, result.getDocumentIdentifier(), "Document identifier should match");
        assertEquals(1, lead.getDocumentDetails().size(), "Lead should have one document detail");
        verify(codeValueMasterService).getByKeys(List.of("TAG_1"));
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void createLeadDocument_withNullDocumentDetails_initializesListAndAdds() {
        // Arrange
        lead.setDocumentDetails(null);
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());
        LeadDocumentCreateRequest request = LeadDocumentCreateRequest.builder()
                .name("doc.pdf").tags(List.of("TAG_1")).build();
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder()
                .id(documentId).identifier(documentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadDocumentCreateResponse result = leadDocumentWriteService.createLeadDocument(leadIdentifier, file, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertNotNull(lead.getDocumentDetails(), "Document details list should be initialized");
        assertEquals(1, lead.getDocumentDetails().size(), "Document details should contain the new document");
    }

    // ==================== createLeadDocument(byte[]) Tests ====================

    @Test
    void createLeadDocument_withByteArray_createsAndReturnsResponse() {
        // Arrange
        byte[] content = "spreadsheet-content".getBytes();
        String filename = "report.xlsx";
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        LeadDocumentCreateRequest request = LeadDocumentCreateRequest.builder()
                .name(filename).tags(List.of("TAG_2")).build();
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder()
                .id(documentId).identifier(documentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentWriteService.createDocument(any(DocumentCreateRequestInputStream.class))).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadDocumentCreateResponse result = leadDocumentWriteService.createLeadDocument(
                leadIdentifier, content, filename, contentType, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(documentIdentifier, result.getDocumentIdentifier(), "Document identifier should match");
        verify(codeValueMasterService).getByKeys(List.of("TAG_2"));
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void createLeadDocument_withByteArrayAndNullDocumentDetails_initializesListAndAdds() {
        // Arrange
        lead.setDocumentDetails(null);
        byte[] content = "content".getBytes();
        LeadDocumentCreateRequest request = LeadDocumentCreateRequest.builder()
                .name("file.csv").tags(List.of("TAG_1")).build();
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder()
                .id(documentId).identifier(documentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentWriteService.createDocument(any(DocumentCreateRequestInputStream.class))).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadDocumentCreateResponse result = leadDocumentWriteService.createLeadDocument(
                leadIdentifier, content, "file.csv", "text/csv", request);

        // Assert
        assertNotNull(lead.getDocumentDetails(), "Document details list should be initialized");
        assertEquals(1, lead.getDocumentDetails().size(), "Should contain the new document detail");
    }

    // ==================== createHouseFrontPhoto() Tests ====================

    @Test
    void createHouseFrontPhoto_withValidJpeg_createsAndReturnsResponse() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "img-bytes".getBytes());
        GeoData geoData = GeoData.builder().build();
        HouseFrontPhotoRequest request = HouseFrontPhotoRequest.builder().geoData(geoData).build();
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder()
                .id(documentId).identifier(documentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadDocumentCreateResponse result = leadDocumentWriteService.createHouseFrontPhoto(leadIdentifier, file, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(documentIdentifier, result.getDocumentIdentifier(), "Document identifier should match");
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void createHouseFrontPhoto_withValidPng_createsAndReturnsResponse() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "img-bytes".getBytes());
        GeoData geoData = GeoData.builder().build();
        HouseFrontPhotoRequest request = HouseFrontPhotoRequest.builder().geoData(geoData).build();
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder()
                .id(documentId).identifier(documentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadDocumentCreateResponse result = leadDocumentWriteService.createHouseFrontPhoto(leadIdentifier, file, request);

        // Assert
        assertNotNull(result, "Response should not be null");
    }

    @Test
    void createHouseFrontPhoto_withInvalidContentType_throwsBadRequestException() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());
        HouseFrontPhotoRequest request = HouseFrontPhotoRequest.builder().geoData(GeoData.builder().build()).build();

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadDocumentWriteService.createHouseFrontPhoto(leadIdentifier, file, request),
                "Should throw when file content type is not JPG/JPEG/PNG");
    }

    @Test
    void createHouseFrontPhoto_withNullContentType_throwsBadRequestException() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", null, "img-bytes".getBytes());
        HouseFrontPhotoRequest request = HouseFrontPhotoRequest.builder().geoData(GeoData.builder().build()).build();

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadDocumentWriteService.createHouseFrontPhoto(leadIdentifier, file, request),
                "Should throw when content type is null");
    }

    @Test
    void createHouseFrontPhoto_updatesPropertyGeoData() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "img-bytes".getBytes());
        GeoData geoData = GeoData.builder().build();
        HouseFrontPhotoRequest request = HouseFrontPhotoRequest.builder().geoData(geoData).build();
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder()
                .id(documentId).identifier(documentIdentifier).build();

        lead.setOtherDetails(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadDocumentWriteService.createHouseFrontPhoto(leadIdentifier, file, request);

        // Assert
        assertNotNull(lead.getOtherDetails(), "OtherDetails should be initialized");
        assertNotNull(lead.getOtherDetails().getPropertyDetails(), "PropertyDetails should be initialized");
        assertEquals(geoData, lead.getOtherDetails().getPropertyDetails().getGeoData(),
                "GeoData should be set from request");
    }

    @Test
    void createHouseFrontPhoto_withExistingOtherDetailsAndNullPropertyDetails_initializesPropertyDetails() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "img-bytes".getBytes());
        GeoData geoData = GeoData.builder().build();
        HouseFrontPhotoRequest request = HouseFrontPhotoRequest.builder().geoData(geoData).build();
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder()
                .id(documentId).identifier(documentIdentifier).build();

        lead.setOtherDetails(Lead.OtherDetails.builder().propertyDetails(null).build());

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadDocumentWriteService.createHouseFrontPhoto(leadIdentifier, file, request);

        // Assert
        assertNotNull(lead.getOtherDetails().getPropertyDetails(),
                "PropertyDetails should be initialized when OtherDetails already exists");
    }

    @Test
    void createHouseFrontPhoto_withNullDocumentDetails_initializesListAndAdds() {
        // Arrange
        lead.setDocumentDetails(null);
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "img-bytes".getBytes());
        HouseFrontPhotoRequest request = HouseFrontPhotoRequest.builder().geoData(GeoData.builder().build()).build();
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder()
                .id(documentId).identifier(documentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadDocumentWriteService.createHouseFrontPhoto(leadIdentifier, file, request);

        // Assert
        assertNotNull(lead.getDocumentDetails(), "Document details should be initialized");
        assertEquals(1, lead.getDocumentDetails().size(), "Should contain the house front photo document");
    }

    // ==================== deleteLeadDocument() Tests ====================

    @Test
    void deleteLeadDocument_withExistingDocument_removesFromLeadAndDeletes() {
        // Arrange
        Lead.DocumentDetail docDetail = Lead.DocumentDetail.builder().id(documentId).tag(List.of("TAG_1")).build();
        lead.setDocumentDetails(new ArrayList<>(List.of(docDetail)));
        DocumentResponse docResponse = DocumentResponse.builder().id(documentId).identifier(documentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentReadService.getDocumentByIdentifier(documentIdentifier)).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadDocumentWriteService.deleteLeadDocument(leadIdentifier, documentIdentifier);

        // Assert
        assertTrue(lead.getDocumentDetails().isEmpty(), "Document detail should be removed from lead");
        verify(documentWriteService).deleteDocumentById(documentIdentifier);
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void deleteLeadDocument_withNullDocumentDetails_deletesDocumentWithoutModifyingLead() {
        // Arrange
        lead.setDocumentDetails(null);
        DocumentResponse docResponse = DocumentResponse.builder().id(documentId).identifier(documentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentReadService.getDocumentByIdentifier(documentIdentifier)).thenReturn(docResponse);

        // Act
        leadDocumentWriteService.deleteLeadDocument(leadIdentifier, documentIdentifier);

        // Assert
        verify(documentWriteService).deleteDocumentById(documentIdentifier);
        verify(leadRepositoryWrapper, never()).saveWithException(any(Lead.class));
    }

    // ==================== updateLeadDocument() Tests ====================

    @Test
    void updateLeadDocument_withValidTags_updatesDocumentDetail() {
        // Arrange
        Lead.DocumentDetail docDetail = Lead.DocumentDetail.builder().id(documentId).tag(List.of("OLD_TAG")).build();
        lead.setDocumentDetails(new ArrayList<>(List.of(docDetail)));
        DocumentResponse docResponse = DocumentResponse.builder().id(documentId).identifier(documentIdentifier).build();
        LeadDocumentUpdateRequest request = LeadDocumentUpdateRequest.builder().tags(List.of("NEW_TAG")).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentReadService.getDocumentByIdentifier(documentIdentifier)).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadDocumentWriteService.updateLeadDocument(leadIdentifier, documentIdentifier, request);

        // Assert
        assertEquals(List.of("NEW_TAG"), docDetail.getTag(), "Tags should be updated to new value");
        verify(codeValueMasterService).getByKeys(List.of("NEW_TAG"));
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void updateLeadDocument_withNullDocumentDetails_throwsBadRequestException() {
        // Arrange
        lead.setDocumentDetails(null);
        DocumentResponse docResponse = DocumentResponse.builder().id(documentId).identifier(documentIdentifier).build();
        LeadDocumentUpdateRequest request = LeadDocumentUpdateRequest.builder().tags(List.of("TAG")).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentReadService.getDocumentByIdentifier(documentIdentifier)).thenReturn(docResponse);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadDocumentWriteService.updateLeadDocument(leadIdentifier, documentIdentifier, request),
                "Should throw when lead has no document details");
    }

    @Test
    void updateLeadDocument_withEmptyDocumentDetails_throwsBadRequestException() {
        // Arrange
        lead.setDocumentDetails(new ArrayList<>());
        DocumentResponse docResponse = DocumentResponse.builder().id(documentId).identifier(documentIdentifier).build();
        LeadDocumentUpdateRequest request = LeadDocumentUpdateRequest.builder().tags(List.of("TAG")).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentReadService.getDocumentByIdentifier(documentIdentifier)).thenReturn(docResponse);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadDocumentWriteService.updateLeadDocument(leadIdentifier, documentIdentifier, request),
                "Should throw when lead has empty document details list");
    }

    @Test
    void updateLeadDocument_withDocumentNotFoundInLead_throwsBadRequestException() {
        // Arrange
        Lead.DocumentDetail otherDoc = Lead.DocumentDetail.builder().id(999L).tag(List.of("OTHER")).build();
        lead.setDocumentDetails(new ArrayList<>(List.of(otherDoc)));
        DocumentResponse docResponse = DocumentResponse.builder().id(documentId).identifier(documentIdentifier).build();
        LeadDocumentUpdateRequest request = LeadDocumentUpdateRequest.builder().tags(List.of("TAG")).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentReadService.getDocumentByIdentifier(documentIdentifier)).thenReturn(docResponse);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadDocumentWriteService.updateLeadDocument(leadIdentifier, documentIdentifier, request),
                "Should throw when document ID is not in lead's document details");
    }

    @Test
    void updateLeadDocument_withNullTags_skipsTagValidation() {
        // Arrange
        Lead.DocumentDetail docDetail = Lead.DocumentDetail.builder().id(documentId).tag(List.of("OLD")).build();
        lead.setDocumentDetails(new ArrayList<>(List.of(docDetail)));
        DocumentResponse docResponse = DocumentResponse.builder().id(documentId).identifier(documentIdentifier).build();
        LeadDocumentUpdateRequest request = LeadDocumentUpdateRequest.builder().tags(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentReadService.getDocumentByIdentifier(documentIdentifier)).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadDocumentWriteService.updateLeadDocument(leadIdentifier, documentIdentifier, request);

        // Assert
        verifyNoInteractions(codeValueMasterService);
        assertNull(docDetail.getTag(), "Tags should be set to null from request");
    }

    @Test
    void updateLeadDocument_withEmptyTags_skipsTagValidation() {
        // Arrange
        Lead.DocumentDetail docDetail = Lead.DocumentDetail.builder().id(documentId).tag(List.of("OLD")).build();
        lead.setDocumentDetails(new ArrayList<>(List.of(docDetail)));
        DocumentResponse docResponse = DocumentResponse.builder().id(documentId).identifier(documentIdentifier).build();
        LeadDocumentUpdateRequest request = LeadDocumentUpdateRequest.builder().tags(List.of()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(documentReadService.getDocumentByIdentifier(documentIdentifier)).thenReturn(docResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadDocumentWriteService.updateLeadDocument(leadIdentifier, documentIdentifier, request);

        // Assert
        verifyNoInteractions(codeValueMasterService);
        assertTrue(docDetail.getTag().isEmpty(), "Tags should be set to empty list from request");
    }
}
