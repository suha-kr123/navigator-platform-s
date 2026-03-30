package com.nivasafinance.features.creditbureau.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parses {@code n_cb_config} entry {@code REDASH_CB_DERIVED_QUERIES} (JSON array of {@code { "queryId": ... }}).
 * Each Redash row: {@code attr_name} or {@code attribute_name}, {@code attr_value} or {@code attribute_value}.
 * Row {@code enquiry_id} is optional; persisted rows use the event enquiry id.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedashDerivedQueryConfig {

    private Long queryId;
}
