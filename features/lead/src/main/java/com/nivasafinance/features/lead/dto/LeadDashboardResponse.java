package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.notes.dto.NotesResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeadDashboardResponse {
    private UUID leadIdentifier;
    private BigDecimal requestedAmount;
    private String productName;
    private String primaryPersonName;
    private String primaryPersonNumber;
    private String officeName;
    private String ownerUsername;
    private LeadStatus status;
    private String recentNote;
    private LocalDateTime leadCreatedAt;
    private LocalDateTime lastActivityDate;
    private String lastActivityBy;
    private String advisor; //todo advisor
    private String leadOwner;
    private LocalTime preferredCallStartTime;
    private LocalTime preferredCallEndTime;
    private CodeValueResponse priority;
    private String partners;

    //TODO :: to add stage, substage, next task data, stageOwner, stageTat
}
