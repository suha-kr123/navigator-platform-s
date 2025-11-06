package com.nivasafinance.features.leadstages.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "n_lead_stage_history")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class LeadStageHistory extends IdentifiableEntity {

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "stage_key", nullable = false, length = 100)
    private String stageKey;

    @Column(name = "stage_from", length = 100)
    private String stageFrom;

    @Column(name = "sub_stage_key", length = 100)
    private String subStageKey;

    @Column(name = "entered_at", nullable = false)
    private LocalDateTime enteredAt;

    @Column(name = "exited_at")
    private LocalDateTime exitedAt;

    @Column(name = "moved_by", length = 255, nullable = false)
    private String movedBy;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    @OneToMany(mappedBy = "stageHistory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LeadStageAssignmentHistory> assignmentHistory = new ArrayList<>();
}

