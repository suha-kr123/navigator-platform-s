package com.nivasafinance.externals.whatsapp.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppNoteRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppNoteResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppNoteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.EXTERNAL_V1 + "/whatsapp")
@AllArgsConstructor
public class WhatsAppNoteController {

    private final WhatsAppNoteService whatsAppNoteService;

    @PostMapping("/notes")
    public ResponseEntity<WhatsAppNoteResponse> createNote(
            @Valid @RequestBody WhatsAppNoteRequest request) {
        WhatsAppNoteResponse response = whatsAppNoteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
