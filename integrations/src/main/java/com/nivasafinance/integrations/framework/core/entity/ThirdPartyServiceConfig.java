package com.nivasafinance.integrations.framework.core.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "f_third_party_service_config")
@org.javers.core.metamodel.annotation.Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ThirdPartyServiceConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "service")
    private String service;

    @Column(name = "primary_config_key")
    private UUID primaryConfigKey;

    @Column(name = "fallback_config_key")
    private UUID fallbackConfigKey;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "is_active")
    private Boolean isActive;
}

