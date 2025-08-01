package audit

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@Suppress("UnnecessaryAbstractClass")
abstract class AuditableEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    var createdBy: String? = null

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedBy
    @Column(name = "updated_by", updatable = true)
    var updatedBy: String? = null

    @LastModifiedDate
    @Column(name = "updated_at", updatable = true)
    var updatedAt: LocalDateTime? = null

    @Version
    @Column(name = "version")
    var version: Long = 0
}
