package com.nivasafinance.features.master.codemaster.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.base.model.MasterLanguageData;
import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "n_master_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MasterCode extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "key", nullable = false, length = 100)
    private String key;
    
    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name", columnDefinition = "jsonb")
    private MasterLanguageData name;
    
    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "jsonb")
    private MasterLanguageData description;
    
    @Column(name = "is_system_defined", nullable = false)
    private Boolean isSystemDefined = false;
}

