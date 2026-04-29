package com.nivasafinance.features.whatsapp.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "n_whatsapp_log_lead")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WhatsappLogLead extends AuditableEntity {

    @Id
    @Column(name = "whatsapp_log_id")
    private Long whatsappLogId;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "contact_id")
    private Long contactId;

    public WhatsappLogLead(Long whatsappLogId, Long leadId, Long contactId) {
        this.whatsappLogId = whatsappLogId;
        this.leadId = leadId;
        this.contactId = contactId;
    }
}
