package com.nivasafinance.features.notes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotesUpdateRequest {
    private String title;
    private String content;
}

