package com.nivasafinance.features.call.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "n_call_log_lead")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CallLogLead extends AuditableEntity {

    @Id
    @Column(name = "call_log_id")
    private Long callLogId;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "contact_id")
    private Long contactId;

    public CallLogLead(Long callLogId, Long leadId, Long contactId) {
        this.callLogId = callLogId;
        this.leadId = leadId;
        this.contactId = contactId;
    }
}
