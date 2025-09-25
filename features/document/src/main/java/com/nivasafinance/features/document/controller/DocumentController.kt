package com.nivasafinance.features.document.controller

import com.nivasafinance.features.document.service.DocumentService
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: DocumentService
) {

    private val logger = LoggerFactory.getLogger(DocumentController::class.java)

    @GetMapping("/{documentId}")
    fun getDocument(@PathVariable documentId: UUID): ResponseEntity<InputStreamResource> {
        try {
            val document = documentService.getDocumentById(documentId)
            val inputStream = documentService.getDocumentStream(documentId)
            
            val headers = HttpHeaders()
            val contentType = document.fileType ?: "application/octet-stream"
            headers.add(HttpHeaders.CONTENT_TYPE, contentType)
            
            val resource = InputStreamResource(inputStream)
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource)
        } catch (e: Exception) {
            logger.error("Error fetching document $documentId", e)
            // Return a proper error response instead of throwing
            return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body(InputStreamResource("{\"error\":\"Failed to fetch document\"}".byteInputStream()))
        }
    }
}