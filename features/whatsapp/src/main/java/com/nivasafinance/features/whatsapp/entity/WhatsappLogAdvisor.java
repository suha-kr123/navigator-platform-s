package com.nivasafinance.features.whatsapp.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "n_whatsapp_log_advisor")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WhatsappLogAdvisor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "whatsapp_log_id", nullable = false, unique = true)
    private Long whatsappLogId;

    @Column(name = "advisor_id", nullable = false)
    private Long advisorId;

    public WhatsappLogAdvisor(Long whatsappLogId, Long advisorId) {
        this.whatsappLogId = whatsappLogId;
        this.advisorId = advisorId;
    }
}
