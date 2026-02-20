package com.nivasafinance.features.master.codemaster.dto;

import com.nivasafinance.common.base.model.PaginatedResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterCodeSearchMultiSectionResponse {

    private PaginatedResponse<MasterCodeSearchResponse> masterMatches;
    private PaginatedResponse<MasterCodeSearchResponse> childMatches;
    private PaginatedResponse<MasterCodeSearchResponse> valueMatches;
}
