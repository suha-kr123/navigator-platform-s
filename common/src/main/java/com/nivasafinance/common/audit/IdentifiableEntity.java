package com.nivasafinance.common.audit;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base entity class for entities that use Long ID with IDENTITY generation strategy.
 * Extends AuditableEntity to include both ID and auditing fields.
 * 
 * Use this class when your entity needs:
 * - Long id field with @GeneratedValue(strategy = GenerationType.IDENTITY)
 * - Automatic auditing (created/updated by, timestamps, version)
 * 
 * For entities that use UUID or other ID types, extend AuditableEntity directly.
 * 
 * Child classes should use @EqualsAndHashCode(callSuper = true) to include
 * audit fields in equals/hashCode calculations.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class IdentifiableEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
