package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvisorTemplateResponse {
    
    private List<CodeValueResponse> advisorRejectionReasons;
    
    private List<CodeValueResponse> occupationTypes;
    
    private List<CodeValueResponse> occupations;
    
    private List<CodeValueResponse> qualifications;
}


