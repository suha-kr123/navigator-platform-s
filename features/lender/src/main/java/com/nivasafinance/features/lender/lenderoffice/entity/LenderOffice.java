package com.nivasafinance.features.lender.lenderoffice.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
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
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "n_lender_office")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class LenderOffice extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @Column(name = "lender_key", nullable = false, length = 20)
    private String lenderKey;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "address", columnDefinition = "jsonb")
    private AddressDetails addressDetails;

    @Column(name = "status", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private LenderOfficeStatus status;
    
    // Nested class for JSONB field (similar to Lead.PropertyDetails)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDetails {
        private AddressData address;
    }
}

