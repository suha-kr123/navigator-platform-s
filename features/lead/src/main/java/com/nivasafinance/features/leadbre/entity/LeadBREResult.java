package com.nivasafinance.features.leadbre.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import com.nivasafinance.features.leadbre.enums.LeadBREResultStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "n_lead_bre_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeadBREResult extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "config_name", nullable = false)
    private String configName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private LeadBREResultStatus status = LeadBREResultStatus.IN_PROGRESS;

    @Column(name = "input")
    private String input;

    @Column(name = "output")
    private String output;

    @PrePersist
    void prePersist() {
        if (identifier == null) {
            identifier = UUID.randomUUID();
        }
    }
}
