package services.voice.entity

import annotations.NoArg
import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "call_logs")
@NoArg
class CallLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "call_sid", length = 50, unique = true, nullable = false)
    var callSid: String,

    @Column(name = "entity_name", length = 50, nullable = false)
    var entityName: String,

    @Column(name = "entity_id", nullable = false)
    var entityId: Long,

    @Column(name = "exophone", length = 20)
    var exophone: String? = null,

    @Column(name = "direction", length = 20, nullable = false)
    var direction: String,

    @Column(name = "from_number", length = 20, nullable = false)
    var fromNumber: String,

    @Column(name = "to_number", length = 20, nullable = false)
    var toNumber: String,

    @Column(name = "status", length = 20, nullable = false)
    var status: String,

    @Column(name = "start_time")
    var startTime: LocalDateTime? = null,

    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,

    @Column(name = "duration")
    var duration: Int? = null,

    @Column(name = "recording_url", columnDefinition = "TEXT")
    var recordingUrl: String? = null

) : AuditableEntity()
