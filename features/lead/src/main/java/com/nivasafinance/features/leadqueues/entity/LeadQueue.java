package com.nivasafinance.features.leadqueues.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.nivasafinance.common.audit.AuditableEntity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "n_lead_queue")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class LeadQueue extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_config_id", nullable = false)
    private Long queueConfigId;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt = LocalDateTime.now();

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "currently_claimed_by", nullable = true)
    private String currentlyClaimedBy;

    @Column(name = "claim_expiry_at", nullable = true)
    private LocalDateTime claimExpiryAt;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "claimed_history", columnDefinition = "jsonb")
    private List<ClaimHistory> claimedHistory;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaimHistory {
        private LocalDateTime claimedAt;
        private String claimedBy;
        private String unclaimedBy;
        private LocalDateTime unclaimedAt;
    }

}
