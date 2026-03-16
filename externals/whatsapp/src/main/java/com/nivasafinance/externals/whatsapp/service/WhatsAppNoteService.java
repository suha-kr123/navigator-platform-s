package com.nivasafinance.externals.whatsapp.service;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppNoteRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppNoteResponse;

public interface WhatsAppNoteService {
    WhatsAppNoteResponse createNote(WhatsAppNoteRequest request);
}
