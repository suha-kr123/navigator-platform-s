package com.nivasafinance.features.document.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.document.enums.DocumentStorageProvider;
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
@TypeName("document")
@Table(name = "n_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Document extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "size")
    private Long size;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private DocumentStorageProvider provider;
    
    @Column(name = "path", nullable = false)
    private String path;
    
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    private Map<String, Object> extData;
}

