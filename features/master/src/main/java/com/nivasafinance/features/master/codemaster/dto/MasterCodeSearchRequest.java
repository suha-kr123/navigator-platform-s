package com.nivasafinance.features.master.codemaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterCodeSearchRequest {

    public static final int MIN_SEARCH_TERM_LENGTH = 3;

    @NotBlank(message = "Search term is required")
    @Size(min = MIN_SEARCH_TERM_LENGTH, message = "Search term must be at least " + MIN_SEARCH_TERM_LENGTH + " characters")
    private String searchTerm;

    @NotEmpty(message = "At least one search context is required")
    private List<SearchContext> searchContexts;

    private String codeKey;
}
