package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.staff.dto.StaffResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeadDashboardFiltersResponse {
    List<OfficeResponse> offices;
    List<StaffResponse> staffs;

    //TODO stages, substages
    //TODO solve circular dependency for getting list of advisors

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OfficeResponse {
        private String name;
        private String key;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StaffResponse {
        private String displayName;
        private String username;
    }
}
