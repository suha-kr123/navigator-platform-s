package com.nivasafinance.integrations.framework.core.entity;

import com.nivasafinance.common.annotations.NoArg;
import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.javers.core.metamodel.annotation.TypeName;

@Entity
@Table(name = "n_third_party_response_log")
@TypeName("third_party_response_log")
@NoArg
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ThirdPartyResponseLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", length = 3, nullable = false)
    private String entityType;

    @Column(name = "entity_id", length = 20, nullable = true)
    private Long entityId;

    @Column(name = "request_method", length = 16, nullable = false)
    private String requestMethod;

    @Column(name = "url", length = 512, nullable = false)
    private String url;

    @Column(name = "request")
    private String request;

    @Column(name = "response")
    private String response;

    @Column(name = "http_status_code", length = 4)
    private Integer httpStatusCode;

    @Column(name = "response_time_ms", length = 20)
    private Long responseTimeInMs;

    @Column(name = "provider_name", length = 50)
    private String providerName;

    @Column(name = "provider_ref_id", length = 50)
    private String providerRefId;

    @Column(name = "business_purpose")
    private String businessPurpose;
}

