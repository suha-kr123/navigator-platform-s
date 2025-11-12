package com.nivasafinance.services.voice.entity;

import com.nivasafinance.common.annotations.NoArg;
import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "call_logs")
@NoArg
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CallLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "call_sid", length = 50, unique = true, nullable = false)
    private String callSid;

    @Column(name = "entity_name", length = 50, nullable = false)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "exophone", length = 20)
    private String exophone;

    @Column(name = "direction", length = 20, nullable = false)
    private String direction;

    @Column(name = "from_number", length = 20, nullable = false)
    private String fromNumber;

    @Column(name = "to_number", length = 20, nullable = false)
    private String toNumber;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "recording_url", columnDefinition = "TEXT")
    private String recordingUrl;
}

