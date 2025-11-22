package com.nivasafinance.features.advisor.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.AdvisorNoteCreateRequest;
import com.nivasafinance.features.advisor.dto.AdvisorNoteCreateResponse;
import com.nivasafinance.features.advisor.dto.AdvisorNoteResponse;
import com.nivasafinance.features.advisor.dto.AdvisorNoteUpdateRequest;
import com.nivasafinance.features.advisor.service.AdvisorNoteReadService;
import com.nivasafinance.features.advisor.service.AdvisorNoteWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/advisors/{advisorId}/notes")
@AllArgsConstructor
public class AdvisorNotesController {

    private final AdvisorNoteWriteService advisorNoteWriteService;
    private final AdvisorNoteReadService advisorNoteReadService;

    @PostMapping
    public ResponseEntity<AdvisorNoteCreateResponse> createAdvisorNote(
            @PathVariable UUID advisorId,
            @Valid @RequestBody AdvisorNoteCreateRequest request
    ) {
        AdvisorNoteCreateResponse response = advisorNoteWriteService.createAdvisorNote(advisorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<AdvisorNoteResponse>> getAllAdvisorNotes(
            @PathVariable UUID advisorId,
            @Valid PaginationRequest paginationRequest) {
        
        PaginatedResponse<AdvisorNoteResponse> notes = advisorNoteReadService.getAllAdvisorNotes(
                advisorId, paginationRequest);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<AdvisorNoteResponse> getAdvisorNoteById(
            @PathVariable UUID advisorId,
            @PathVariable UUID noteId) {
        AdvisorNoteResponse note = advisorNoteReadService.getAdvisorNoteById(advisorId, noteId);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<Void> updateAdvisorNote(
            @PathVariable UUID advisorId,
            @PathVariable UUID noteId,
            @Valid @RequestBody AdvisorNoteUpdateRequest request) {
        advisorNoteWriteService.updateAdvisorNote(advisorId, noteId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteAdvisorNote(
            @PathVariable UUID advisorId,
            @PathVariable UUID noteId) {
        advisorNoteWriteService.deleteAdvisorNote(advisorId, noteId);
        return ResponseEntity.noContent().build();
    }
}

