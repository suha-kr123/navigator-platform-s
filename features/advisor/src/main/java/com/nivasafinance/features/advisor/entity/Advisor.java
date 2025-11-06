package com.nivasafinance.features.advisor.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.javers.core.metamodel.annotation.TypeName;

import java.util.Map;
import java.util.UUID;

@Entity
@TypeName("advisor")
@Table(name = "advisor")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Advisor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "advisor_code")
    private String advisorCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AdvisorStatus status;

    @Column(name = "rejection_reason_key", length = 50)
    private String rejectionReasonKey;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    private Map<String, Object> extData;
}

