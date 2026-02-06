package com.nivasafinance.features.creditbureau.dto;

import com.nivasafinance.features.creditbureau.entity.CreditBureauTrends;
import com.nivasafinance.features.creditbureau.enums.TrendName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendsResponse {
    private UUID identifier;
    private TrendName trendName;
    private LocalDate date;
    private Integer scoreValue;
    private String description;

    public static TrendsResponse toTrendsResponse(CreditBureauTrends trends) {
        if (trends == null) {
            return null;
        }
        return TrendsResponse.builder()
                .identifier(trends.getIdentifier())
                .trendName(TrendName.fromString(trends.getTrendName()))
                .date(trends.getDate())
                .scoreValue(trends.getScoreValue())
                .description(trends.getDescription())
                .build();
    }
}
