package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.lead.dto.LeadNoteCreateRequest;
import com.nivasafinance.features.lead.dto.LeadNoteCreateResponse;
import com.nivasafinance.features.lead.dto.LeadNoteResponse;
import com.nivasafinance.features.lead.dto.LeadNoteUpdateRequest;
import com.nivasafinance.features.lead.service.LeadNoteReadService;
import com.nivasafinance.features.lead.service.LeadNoteWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadId}/notes")
@AllArgsConstructor
public class LeadNotesController {

    private final LeadNoteWriteService leadNoteWriteService;
    private final LeadNoteReadService leadNoteReadService;

    @PostMapping
    public ResponseEntity<LeadNoteCreateResponse> createLeadNote(
            @PathVariable UUID leadId,
            @Valid @RequestBody LeadNoteCreateRequest request
    ) {
        LeadNoteCreateResponse response = leadNoteWriteService.createLeadNote(leadId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LeadNoteResponse>> getAllLeadNotes(@PathVariable UUID leadId) {
        List<LeadNoteResponse> notes = leadNoteReadService.getAllLeadNotes(leadId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<LeadNoteResponse> getLeadNoteById(
            @PathVariable UUID leadId,
            @PathVariable UUID noteId) {
        LeadNoteResponse note = leadNoteReadService.getLeadNoteById(leadId, noteId);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<Void> updateLeadNote(
            @PathVariable UUID leadId,
            @PathVariable UUID noteId,
            @Valid @RequestBody LeadNoteUpdateRequest request) {
        leadNoteWriteService.updateLeadNote(leadId, noteId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteLeadNote(
            @PathVariable UUID leadId,
            @PathVariable UUID noteId) {
        leadNoteWriteService.deleteLeadNote(leadId, noteId);
        return ResponseEntity.noContent().build();
    }
}

