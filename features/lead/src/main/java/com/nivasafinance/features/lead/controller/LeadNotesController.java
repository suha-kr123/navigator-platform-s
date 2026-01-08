package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
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

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadId}/notes")
@AllArgsConstructor
public class LeadNotesController {

    private final LeadNoteWriteService leadNoteWriteService;
    private final LeadNoteReadService leadNoteReadService;

    @PostMapping
    @RequirePermission(permissionName = "CREATE_LEAD_NOTES")
    public ResponseEntity<LeadNoteCreateResponse> createLeadNote(
            @PathVariable UUID leadId,
            @Valid @RequestBody LeadNoteCreateRequest request
    ) {
        LeadNoteCreateResponse response = leadNoteWriteService.createLeadNote(leadId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @RequirePermission(permissionName = "READ_LEAD_NOTES")
    public ResponseEntity<PaginatedResponse<LeadNoteResponse>> getAllLeadNotes(
            @PathVariable UUID leadId,
            @Valid PaginationRequest paginationRequest) {
        
        PaginatedResponse<LeadNoteResponse> notes = leadNoteReadService.getAllLeadNotes(
                leadId, paginationRequest);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{noteId}")
    @RequirePermission(permissionName = "READ_LEAD_NOTES")
    public ResponseEntity<LeadNoteResponse> getLeadNoteById(
            @PathVariable UUID leadId,
            @PathVariable UUID noteId) {
        LeadNoteResponse note = leadNoteReadService.getLeadNoteById(leadId, noteId);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/{noteId}")
    @RequirePermission(permissionName = "UPDATE_LEAD_NOTES")
    public ResponseEntity<Void> updateLeadNote(
            @PathVariable UUID leadId,
            @PathVariable UUID noteId,
            @Valid @RequestBody LeadNoteUpdateRequest request) {
        leadNoteWriteService.updateLeadNote(leadId, noteId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{noteId}")
    @RequirePermission(permissionName = "DELETE_LEAD_NOTES")
    public ResponseEntity<Void> deleteLeadNote(
            @PathVariable UUID leadId,
            @PathVariable UUID noteId) {
        leadNoteWriteService.deleteLeadNote(leadId, noteId);
        return ResponseEntity.noContent().build();
    }
}

