package com.nivasafinance.integrations.framework.core.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "n_third_party_service_config")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ThirdPartyServiceConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "service")
    private String service;

    @Column(name = "primary_config" , nullable = false)
    private Long primaryConfigId;

    @Column(name = "fallback_config")
    private Long fallbackConfigId;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "is_active")
    private Boolean isActive;
}

