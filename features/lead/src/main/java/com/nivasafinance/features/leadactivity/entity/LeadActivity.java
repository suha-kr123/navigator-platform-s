package com.nivasafinance.features.leadactivity.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.leadactivity.enums.ResourceAction;
import com.nivasafinance.features.leadactivity.enums.ResourceEnum;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "n_lead_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class LeadActivity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type")
    private ResourceEnum resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_action")
    private ResourceAction resourceAction;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "description")
    private String description;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}
