package com.nivasafinance.features.document.service.impl;

import com.nivasafinance.features.document.dto.DocumentFileResponse;
import com.nivasafinance.features.document.dto.DocumentResponse;
import com.nivasafinance.features.document.entity.Document;
import com.nivasafinance.features.document.enums.DocumentStorageProvider;
import com.nivasafinance.features.document.repository.DocumentRepositoryWrapper;
import com.nivasafinance.features.document.storage.ContentRepository;
import com.nivasafinance.features.document.storage.ContentRepositoryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentReadServiceImplTest {

    @Mock
    private DocumentRepositoryWrapper documentRepositoryWrapper;

    @Mock
    private ContentRepositoryFactory contentRepositoryFactory;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private DocumentReadServiceImpl documentReadService;

    private static final Long TEST_DOC_ID = 1L;

    private UUID testIdentifier;
    private Document document;

    @BeforeEach
    void setUp() {
        testIdentifier = UUID.randomUUID();

        document = new Document();
        document.setId(TEST_DOC_ID);
        document.setIdentifier(testIdentifier);
        document.setName("test-doc.pdf");
        document.setType("application/pdf");
        document.setSize(1024L);
        document.setProvider(DocumentStorageProvider.LOCAL);
        document.setPath("documents/test-doc.pdf");
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        document.setCreatedBy("test-user");
        document.setUpdatedBy("test-user");
    }

    // ── getDocumentById ──────────────────────────────────────────────

    @Test
    void getDocumentById_existingId_returnsDocumentResponse() {
        when(documentRepositoryWrapper.findByIdWithException(TEST_DOC_ID)).thenReturn(document);

        DocumentResponse result = documentReadService.getDocumentById(TEST_DOC_ID);

        assertNotNull(result, "Response should not be null for an existing document ID");
        assertEquals(TEST_DOC_ID, result.getId(), "Returned document ID should match the requested ID");
        assertEquals("test-doc.pdf", result.getName(), "Document name should be mapped correctly");
    }

    @Test
    void getDocumentById_existingId_mapsAllFields() {
        when(documentRepositoryWrapper.findByIdWithException(TEST_DOC_ID)).thenReturn(document);

        DocumentResponse result = documentReadService.getDocumentById(TEST_DOC_ID);

        assertEquals(testIdentifier, result.getIdentifier(), "Identifier should be mapped from entity");
        assertEquals("application/pdf", result.getType(), "Type should be mapped from entity");
        assertEquals(1024L, result.getSize(), "Size should be mapped from entity");
        assertNotNull(result.getCreatedAt(), "CreatedAt should be mapped from entity");
        assertEquals("test-user", result.getCreatedBy(), "CreatedBy should be mapped from entity");
    }

    @Test
    void getDocumentById_verifiesRepositoryInteraction() {
        when(documentRepositoryWrapper.findByIdWithException(TEST_DOC_ID)).thenReturn(document);

        documentReadService.getDocumentById(TEST_DOC_ID);

        verify(documentRepositoryWrapper).findByIdWithException(TEST_DOC_ID);
        verifyNoInteractions(contentRepositoryFactory);
    }

    // ── getDocumentByIdentifier ──────────────────────────────────────

    @Test
    void getDocumentByIdentifier_existingUuid_returnsDocumentResponse() {
        when(documentRepositoryWrapper.findByIdentifierWithException(testIdentifier)).thenReturn(document);

        DocumentResponse result = documentReadService.getDocumentByIdentifier(testIdentifier);

        assertNotNull(result, "Response should not be null for an existing document identifier");
        assertEquals(testIdentifier, result.getIdentifier(),
                "Returned identifier should match the requested UUID");
    }

    @Test
    void getDocumentByIdentifier_verifiesRepositoryInteraction() {
        when(documentRepositoryWrapper.findByIdentifierWithException(testIdentifier)).thenReturn(document);

        documentReadService.getDocumentByIdentifier(testIdentifier);

        verify(documentRepositoryWrapper).findByIdentifierWithException(testIdentifier);
        verify(documentRepositoryWrapper, never()).findByIdWithException(anyLong());
    }

    // ── getDocumentFile (by Long id) ─────────────────────────────────

    @Test
    void getDocumentFileById_existingId_returnsFileWithMetadata() {
        InputStream mockStream = new ByteArrayInputStream("file-content".getBytes());
        when(documentRepositoryWrapper.findByIdWithException(TEST_DOC_ID)).thenReturn(document);
        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);
        when(contentRepository.fetchFile("documents/test-doc.pdf")).thenReturn(mockStream);

        DocumentFileResponse result = documentReadService.getDocumentFile(TEST_DOC_ID);

        assertNotNull(result, "File response should not be null");
        assertNotNull(result.getFile(), "File input stream should not be null");
        assertNotNull(result.getData(), "Document metadata should not be null");
        assertEquals(TEST_DOC_ID, result.getData().getId(),
                "Metadata document ID should match the requested ID");
    }

    @Test
    void getDocumentFileById_resolvesProviderFromEntity() {
        InputStream mockStream = new ByteArrayInputStream(new byte[0]);
        when(documentRepositoryWrapper.findByIdWithException(TEST_DOC_ID)).thenReturn(document);
        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);
        when(contentRepository.fetchFile("documents/test-doc.pdf")).thenReturn(mockStream);

        documentReadService.getDocumentFile(TEST_DOC_ID);

        verify(contentRepositoryFactory).getRepository("LOCAL");
        verify(contentRepository).fetchFile("documents/test-doc.pdf");
    }

    @Test
    void getDocumentFileById_s3Provider_resolvesS3Repository() {
        document.setProvider(DocumentStorageProvider.AWS_S3);
        document.setPath("s3://bucket/test-doc.pdf");
        InputStream mockStream = new ByteArrayInputStream(new byte[0]);

        when(documentRepositoryWrapper.findByIdWithException(TEST_DOC_ID)).thenReturn(document);
        when(contentRepositoryFactory.getRepository("AWS_S3")).thenReturn(contentRepository);
        when(contentRepository.fetchFile("s3://bucket/test-doc.pdf")).thenReturn(mockStream);

        DocumentFileResponse result = documentReadService.getDocumentFile(TEST_DOC_ID);

        assertNotNull(result.getFile(), "File stream should not be null for S3 provider");
        verify(contentRepositoryFactory).getRepository("AWS_S3");
    }

    // ── getDocumentFile (by UUID) ────────────────────────────────────

    @Test
    void getDocumentFileByUuid_existingIdentifier_returnsFileWithMetadata() {
        InputStream mockStream = new ByteArrayInputStream("content".getBytes());
        when(documentRepositoryWrapper.findByIdentifierWithException(testIdentifier)).thenReturn(document);
        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);
        when(contentRepository.fetchFile("documents/test-doc.pdf")).thenReturn(mockStream);

        DocumentFileResponse result = documentReadService.getDocumentFile(testIdentifier);

        assertNotNull(result, "File response should not be null");
        assertNotNull(result.getFile(), "File input stream should not be null");
        assertEquals(testIdentifier, result.getData().getIdentifier(),
                "Metadata identifier should match the requested UUID");
    }

    @Test
    void getDocumentFileByUuid_verifiesCorrectRepositoryLookup() {
        InputStream mockStream = new ByteArrayInputStream(new byte[0]);
        when(documentRepositoryWrapper.findByIdentifierWithException(testIdentifier)).thenReturn(document);
        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);
        when(contentRepository.fetchFile("documents/test-doc.pdf")).thenReturn(mockStream);

        documentReadService.getDocumentFile(testIdentifier);

        verify(documentRepositoryWrapper).findByIdentifierWithException(testIdentifier);
        verify(documentRepositoryWrapper, never()).findByIdWithException(anyLong());
    }
}
