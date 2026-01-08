package com.nivasafinance.redash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedashQueryResponse {

    private Job job;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Job{
        private String id;
        private Long status;
        @JsonProperty("query_result_id")
        private String queryResultId;
        private String error;
    }
}
