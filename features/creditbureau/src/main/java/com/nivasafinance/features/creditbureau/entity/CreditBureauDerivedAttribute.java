package com.nivasafinance.features.creditbureau.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "n_cb_derived_attribute")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreditBureauDerivedAttribute extends IdentifiableEntity {

    @Column(name = "enquiry_id", nullable = false)
    private Long enquiryId;

    @Column(name = "attr_name", nullable = false, length = 200)
    private String attrName;

    @Column(name = "attr_value", columnDefinition = "TEXT")
    private String attrValue;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    private Map<String, Object> dataExt;
}
