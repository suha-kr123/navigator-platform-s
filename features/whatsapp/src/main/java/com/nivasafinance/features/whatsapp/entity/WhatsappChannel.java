package com.nivasafinance.features.whatsapp.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.whatsapp.enums.WhatsappChannelStatus;
import com.nivasafinance.features.whatsapp.enums.WhatsappEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "n_whatsapp_channel")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class WhatsappChannel extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false, unique = true, length = 100)
    private String channelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity", nullable = false, length = 50)
    private WhatsappEntity entity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private WhatsappChannelStatus status;
}
