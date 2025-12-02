package com.nivasafinance.features.advisoractivity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAdvisorActivityResponse {
    private Long id;
    private UUID identifier;
}

