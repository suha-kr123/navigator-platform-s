package audit

import jakarta.persistence.*
import jakarta.persistence.Version // audit
import org.springframework.data.annotation.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
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
