package com.nivasafinance.services.whatsapp.entity;

import com.nivasafinance.common.annotations.NoArg;
import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "whatsapp_logs")
@NoArg
@Data
public class WhatsAppLog extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "message_id", length = 100, unique = true, nullable = false)
    private String messageId;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;
    
    @Column(name = "template_name", length = 100)
    private String templateName;
    
    @Column(name = "broadcast_name", length = 100)
    private String broadcastName;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    
    @Column(name = "sent_time")
    private LocalDateTime sentTime;
    
    @Column(name = "delivered_time")
    private LocalDateTime deliveredTime;
}

