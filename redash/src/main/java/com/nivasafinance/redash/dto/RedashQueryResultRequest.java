package com.nivasafinance.redash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedashQueryResultRequest {
    
    private Long queryId;
    @JsonProperty("max_age")
    private Long maxAge;
    private Map<String, Object> parameters;

}

