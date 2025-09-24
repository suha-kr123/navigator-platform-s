package com.nivasafinance.features.document.controller

import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.dto.DocumentVerificationRequest
import com.nivasafinance.features.document.service.DocumentService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: DocumentService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createDocument(
        @RequestParam("entityId") entityId: UUID,
        @RequestParam("entityType") entityType: String,
        @RequestParam("documentType") documentType: String,
        @RequestParam("category") category: String? = null,
        @RequestParam("docType") docType: String? = null,
        @RequestParam("tags") tags: List<String>? = null,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<DocumentResponse> {
        val documentRequest = DocumentRequest(
            entityId = entityId,
            entityType = entityType,
            documentType = documentType,
            fileName = file.originalFilename ?: "unknown",
            fileType = file.contentType,
            fileSize = file.size,
            category = category,
            docType = docType,
            tags = tags
        )

        val document = documentService.createDocument(documentRequest, file.inputStream)
        return ResponseEntity.status(HttpStatus.CREATED).body(document)
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    fun getDocumentsByEntityTypeAndEntityId(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID
    ): ResponseEntity<List<DocumentResponse>> {
        val documents = documentService.getDocumentsByEntityTypeAndEntityId(entityType, entityId)
        return ResponseEntity.ok(documents)
    }

    @GetMapping("/{id}")
    fun getDocumentById(@PathVariable id: UUID): ResponseEntity<DocumentResponse> {
        val document = documentService.getDocumentById(id)
        return ResponseEntity.ok(document)
    }

    @DeleteMapping("/{id}")
    fun deleteDocumentById(@PathVariable id: UUID): ResponseEntity<Void> {
        documentService.deleteDocumentById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/download")
    fun downloadDocument(@PathVariable id: UUID): ResponseEntity<Resource> {
        val document = documentService.getDocumentById(id)
        val resource = documentService.downloadDocument(id)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${document.fileName}\"")
            .header(HttpHeaders.CONTENT_TYPE, document.fileType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .body(resource)
    }

    @GetMapping("/{id}/download-url")
    fun getDocumentDownloadUrl(
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "3600") expiresIn: Long
    ): ResponseEntity<Map<String, String>> {
        val downloadUrl = documentService.getDocumentDownloadUrl(id, expiresIn)
        return ResponseEntity.ok(mapOf("downloadUrl" to downloadUrl))
    }

    @PutMapping("/{id}/verify")
    fun verifyDocument(
        @PathVariable id: UUID,
        @RequestBody verificationRequest: DocumentVerificationRequest
    ): ResponseEntity<DocumentResponse> {
        val document = documentService.verifyDocument(id, verificationRequest)
        return ResponseEntity.ok(document)
    }
}
