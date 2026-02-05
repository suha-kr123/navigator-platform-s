package com.nivasafinance.notification.orchestrator.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.notification.orchestrator.enums.Platform;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "n_device")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Device extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "app_user", length = 50, nullable = false)
    private String appUser;

    @Column(name = "notification_token", length = 500, nullable = false)
    private String notificationToken;

    @Column(name = "device_id", length = 255 ,nullable = false)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 50, nullable = false)
    private Platform platform;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "apk_version", length = 50)
    private String apkVersion;

    @Column(name = "sdk_version", length = 50)
    private String sdkVersion;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_registration_token_time")
    private Instant lastRegistrationTokenTime;
}

