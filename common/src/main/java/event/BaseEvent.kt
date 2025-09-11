package event

import java.time.LocalDateTime
import java.util.UUID

interface BaseEvent {
    val eventId: UUID
    val timestamp: LocalDateTime
    val eventType: String
    val source: String
}

interface BaseDomainEvent : BaseEvent {
    val aggregateId: String
    val aggregateType: String
    val version: Int
}
