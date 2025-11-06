package com.nivasafinance.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base class for all entities that need auditing.
 * Provides automatic tracking of creation and modification timestamps and users.
 * 
 * For entities that use Long ID with IDENTITY generation, extend {@link IdentifiableEntity} instead.
 * For entities that use UUID or other ID types, extend this class directly.
 * 
 * Child classes should use @EqualsAndHashCode(callSuper = true) to include
 * audit fields in equals/hashCode calculations.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public abstract class AuditableEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", updatable = true)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", updatable = true)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version = 0L;
}

