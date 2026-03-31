package com.nivasafinance.features.displayconfig.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import com.nivasafinance.features.displayconfig.enums.AppType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "n_app_display_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppDisplayConfig extends IdentifiableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "app_type", nullable = false, unique = true, length = 50)
    private AppType appType;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> config;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
