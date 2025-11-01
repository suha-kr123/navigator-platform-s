package com.nivasafinance.features.notes.dto;

import com.nivasafinance.features.notes.entity.Notes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotesResponse {
    private Long id;
    private UUID identifier;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

     public static NotesResponse toNotesResponse(Notes notes) {
        return new NotesResponse(
                notes.getId(),
                notes.getIdentifier(),
                notes.getTitle(),
                notes.getContent(),
                notes.getCreatedAt(),
                notes.getUpdatedAt(),
                notes.getCreatedBy(),
                notes.getUpdatedBy()
        );
    }
}

