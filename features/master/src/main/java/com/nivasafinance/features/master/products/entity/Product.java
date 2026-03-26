package com.nivasafinance.features.master.products.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.base.model.MasterLanguageData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "n_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name", columnDefinition = "jsonb", nullable = false)
    private MasterLanguageData name;
}
