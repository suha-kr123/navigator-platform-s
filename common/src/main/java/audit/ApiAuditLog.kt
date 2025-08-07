package audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "api_audit_log")
data class ApiAuditLog(

    @Id
    val id: UUID? = UUID.randomUUID(),

    @Column(name = "username")
    val username: String? = null,

    @Column(name = "method")
    val method: String,

    @Column(name = "uri")
    val uri: String,

    @Column(name = "ip_address")
    val ipAddress: String? = null,

    @Column(name = "user_agent")
    val userAgent: String? = null,

    @Column(name = "request_body", columnDefinition = "TEXT")
    val requestBody: String? = null,

    @Column(name = "response_body", columnDefinition = "TEXT")
    val responseBody: String? = null,

    @Column(name = "response_status")
    val responseStatus: Int,

    @Column(name = "error_message", nullable = true)
    val errorMessage: String? = null,

    @Column(name = "duration_ms")
    val durationMs: Long,

    @Column(name = "timestamp")
    val timestamp: LocalDateTime = LocalDateTime.now()
)
