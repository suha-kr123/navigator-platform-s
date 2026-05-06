package com.nivasafinance.features.master.location.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.base.model.MasterLanguageData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "n_master_taluka")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Taluka extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "district_id", nullable = false)
    private Long districtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", insertable = false, updatable = false)
    private District district;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", length = 50)
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "jsonb")
    private MasterLanguageData nameValues;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_serviceable", nullable = false)
    private Boolean isServiceable = false;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
}

