package com.nivasafinance.features.bre.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;


@Entity
@Table(name = "n_bre_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BRELogs extends IdentifiableEntity {

    @Column(name = "data_ext")
    private String dataExt;

    @Column(name = "config_id")
    private Long breConfigId;

    @Column(name = "request")
    private String request;

    @Column(name = "response")
    private String response;
}