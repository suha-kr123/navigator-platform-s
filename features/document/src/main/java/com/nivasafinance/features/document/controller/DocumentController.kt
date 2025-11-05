package com.nivasafinance.features.document.controller

import com.nivasafinance.common.constants.ApiConstants
import com.nivasafinance.features.document.service.DocumentReadService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiConstants.V1 + "/documents")
class DocumentController(
    private val documentReadService: DocumentReadService
) {

    @GetMapping("/{documentId}")
    fun getDocument(@PathVariable documentId: UUID): ResponseEntity<InputStreamResource> {
        val documentFileResponse = documentReadService.getDocumentFile(documentId)

        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_TYPE, documentFileResponse.data.type)
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${documentFileResponse.data.name}\"")

        val resource = InputStreamResource(documentFileResponse.file)
        return ResponseEntity.ok()
            .headers(headers)
            .contentType(
                MediaType.parseMediaType(
                    documentFileResponse.data.type ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
                )
            )
            .body(resource)
    }
}
