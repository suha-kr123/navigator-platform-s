package com.nivasafinance.features.document.service.impl;

import com.nivasafinance.features.document.config.DocumentStorageProperties;
import com.nivasafinance.features.document.dto.DocumentCreateRequest;
import com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.entity.Document;
import com.nivasafinance.features.document.enums.DocumentStorageProvider;
import com.nivasafinance.features.document.exception.DocumentOperationException;
import com.nivasafinance.features.document.exception.DocumentValidationException;
import com.nivasafinance.features.document.repository.DocumentRepositoryWrapper;
import com.nivasafinance.features.document.storage.ContentRepository;
import com.nivasafinance.features.document.storage.ContentRepositoryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentWriteServiceImplTest {

    @Mock
    private DocumentRepositoryWrapper documentRepositoryWrapper;

    @Mock
    private ContentRepositoryFactory contentRepositoryFactory;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private MessageSource messageSource;

    private DocumentStorageProperties documentStorageProperties;

    private DocumentWriteServiceImpl documentWriteService;

    private static final Long TEST_DOC_ID = 1L;

    private UUID testIdentifier;
    private Document document;

    @BeforeEach
    void setUp() {
        documentStorageProperties = new DocumentStorageProperties();
        documentStorageProperties.setProvider("LOCAL");

        documentWriteService = new DocumentWriteServiceImpl(
                documentRepositoryWrapper, contentRepositoryFactory,
                documentStorageProperties, messageSource);

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
    }

    // ── createDocument (MultipartFile) ───────────────────────────────

    @Test
    void createDocument_multipartFile_happyPath_returnsResponse() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(1024L);

        DocumentCreateRequest request = DocumentCreateRequest.builder()
                .name("test-doc.pdf")
                .file(file)
                .build();

        Document savedDoc = new Document();
        savedDoc.setId(TEST_DOC_ID);
        savedDoc.setIdentifier(testIdentifier);

        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);
        when(contentRepository.saveFile(any(InputStream.class), anyString())).thenReturn("documents/stored-path");
        when(documentRepositoryWrapper.saveWithException(any(Document.class))).thenReturn(savedDoc);

        DocumentCreateResponse result = documentWriteService.createDocument(request);

        assertNotNull(result, "Create response should not be null");
        assertEquals(TEST_DOC_ID, result.getId(), "Response ID should match the saved document ID");
        assertEquals(testIdentifier, result.getIdentifier(),
                "Response identifier should match the saved document identifier");
    }

    @Test
    void createDocument_multipartFile_withCustomPath_usesCustomPath() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(512L);

        DocumentCreateRequest request = DocumentCreateRequest.builder()
                .name("icon.png")
                .file(file)
                .customPath("assets/icons/icon.png")
                .build();

        Document savedDoc = new Document();
        savedDoc.setId(2L);
        savedDoc.setIdentifier(UUID.randomUUID());

        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);
        when(contentRepository.saveFile(any(InputStream.class), eq("assets/icons/icon.png")))
                .thenReturn("assets/icons/icon.png");
        when(documentRepositoryWrapper.saveWithException(any(Document.class))).thenReturn(savedDoc);

        documentWriteService.createDocument(request);

        verify(contentRepository).saveFile(any(InputStream.class), eq("assets/icons/icon.png"));
    }

    @Test
    void createDocument_multipartFile_nullFile_throwsValidation() {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Test error message");

        DocumentCreateRequest request = DocumentCreateRequest.builder()
                .name("test.pdf")
                .file(null)
                .build();

        assertThrows(DocumentValidationException.class,
                () -> documentWriteService.createDocument(request),
                "Null file should throw DocumentValidationException");
    }

    @Test
    void createDocument_multipartFile_nullName_throwsValidation() {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Test error message");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        DocumentCreateRequest request = DocumentCreateRequest.builder()
                .name(null)
                .file(file)
                .build();

        assertThrows(DocumentValidationException.class,
                () -> documentWriteService.createDocument(request),
                "Null name should throw DocumentValidationException");
    }

    @Test
    void createDocument_multipartFile_ioException_throwsOperationException() throws IOException {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Test error message");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenThrow(new IOException("read error"));

        DocumentCreateRequest request = DocumentCreateRequest.builder()
                .name("test.pdf")
                .file(file)
                .build();

        assertThrows(DocumentOperationException.class,
                () -> documentWriteService.createDocument(request),
                "IOException during file read should throw DocumentOperationException");
    }

    // ── createDocument (InputStream) ─────────────────────────────────

    @Test
    void createDocument_inputStream_happyPath_returnsResponse() {
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());
        DocumentCreateRequestInputStream request = DocumentCreateRequestInputStream.builder()
                .name("stream-doc.pdf")
                .file(inputStream)
                .contentType("application/pdf")
                .size(100L)
                .build();

        Document savedDoc = new Document();
        savedDoc.setId(TEST_DOC_ID);
        savedDoc.setIdentifier(testIdentifier);

        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);
        when(contentRepository.saveFile(any(InputStream.class), anyString())).thenReturn("documents/stored");
        when(documentRepositoryWrapper.saveWithException(any(Document.class))).thenReturn(savedDoc);

        DocumentCreateResponse result = documentWriteService.createDocument(request);

        assertNotNull(result, "Create response should not be null for InputStream-based creation");
        assertEquals(TEST_DOC_ID, result.getId(), "Response ID should match the saved document ID");
    }

    @Test
    void createDocument_inputStream_withCustomPath_usesCustomPath() {
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());
        DocumentCreateRequestInputStream request = DocumentCreateRequestInputStream.builder()
                .name("custom.pdf")
                .file(inputStream)
                .customPath("custom/path/doc.pdf")
                .contentType("application/pdf")
                .size(200L)
                .build();

        Document savedDoc = new Document();
        savedDoc.setId(3L);
        savedDoc.setIdentifier(UUID.randomUUID());

        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);
        when(contentRepository.saveFile(any(InputStream.class), eq("custom/path/doc.pdf")))
                .thenReturn("custom/path/doc.pdf");
        when(documentRepositoryWrapper.saveWithException(any(Document.class))).thenReturn(savedDoc);

        documentWriteService.createDocument(request);

        verify(contentRepository).saveFile(any(InputStream.class), eq("custom/path/doc.pdf"));
    }

    @Test
    void createDocument_inputStream_nullFile_throwsValidation() {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Test error message");

        DocumentCreateRequestInputStream request = DocumentCreateRequestInputStream.builder()
                .name("test.pdf")
                .file(null)
                .build();

        assertThrows(DocumentValidationException.class,
                () -> documentWriteService.createDocument(request),
                "Null InputStream should throw DocumentValidationException");
    }

    @Test
    void createDocument_inputStream_blankName_throwsValidation() {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Test error message");

        DocumentCreateRequestInputStream request = DocumentCreateRequestInputStream.builder()
                .name("   ")
                .file(new ByteArrayInputStream(new byte[0]))
                .build();

        assertThrows(DocumentValidationException.class,
                () -> documentWriteService.createDocument(request),
                "Blank name should throw DocumentValidationException");
    }

    @Test
    void createDocument_inputStream_setsProviderFromConfig() {
        documentStorageProperties.setProvider("AWS_S3");
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());
        DocumentCreateRequestInputStream request = DocumentCreateRequestInputStream.builder()
                .name("s3-doc.pdf")
                .file(inputStream)
                .contentType("application/pdf")
                .size(300L)
                .build();

        Document savedDoc = new Document();
        savedDoc.setId(4L);
        savedDoc.setIdentifier(UUID.randomUUID());

        when(contentRepositoryFactory.getRepository("AWS_S3")).thenReturn(contentRepository);
        when(contentRepository.saveFile(any(InputStream.class), anyString())).thenReturn("s3-key");
        when(documentRepositoryWrapper.saveWithException(any(Document.class))).thenReturn(savedDoc);

        documentWriteService.createDocument(request);

        verify(contentRepositoryFactory).getRepository("AWS_S3");
    }

    // ── deleteDocumentById (UUID) ────────────────────────────────────

    @Test
    void deleteDocumentByUuid_existingDocument_deletesFromStorageAndDb() {
        when(documentRepositoryWrapper.findByIdentifierWithException(testIdentifier)).thenReturn(document);
        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);

        documentWriteService.deleteDocumentById(testIdentifier);

        verify(contentRepository).deleteFile("documents/test-doc.pdf");
        verify(documentRepositoryWrapper).deleteByIdWithException(TEST_DOC_ID);
    }

    @Test
    void deleteDocumentByUuid_verifiesDeletionOrder_storageBeforeDb() {
        when(documentRepositoryWrapper.findByIdentifierWithException(testIdentifier)).thenReturn(document);
        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);

        documentWriteService.deleteDocumentById(testIdentifier);

        var inOrder = inOrder(contentRepository, documentRepositoryWrapper);
        inOrder.verify(contentRepository).deleteFile("documents/test-doc.pdf");
        inOrder.verify(documentRepositoryWrapper).deleteByIdWithException(TEST_DOC_ID);
    }

    // ── deleteDocumentById (Long) ────────────────────────────────────

    @Test
    void deleteDocumentByLongId_existingDocument_deletesFromStorageAndDb() {
        when(documentRepositoryWrapper.findByIdWithException(TEST_DOC_ID)).thenReturn(document);
        when(contentRepositoryFactory.getRepository("LOCAL")).thenReturn(contentRepository);

        documentWriteService.deleteDocumentById(TEST_DOC_ID);

        verify(contentRepository).deleteFile("documents/test-doc.pdf");
        verify(documentRepositoryWrapper).deleteByIdWithException(TEST_DOC_ID);
    }

    @Test
    void deleteDocumentByLongId_resolvesProviderFromEntity() {
        document.setProvider(DocumentStorageProvider.AWS_S3);
        when(documentRepositoryWrapper.findByIdWithException(TEST_DOC_ID)).thenReturn(document);
        when(contentRepositoryFactory.getRepository("AWS_S3")).thenReturn(contentRepository);

        documentWriteService.deleteDocumentById(TEST_DOC_ID);

        verify(contentRepositoryFactory).getRepository("AWS_S3");
    }
}
