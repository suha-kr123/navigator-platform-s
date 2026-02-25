package com.nivasafinance.features.staff.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "n_staff")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Staff extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier", nullable = false, unique = true, updatable = false)
    private UUID identifier;

    @Column(name = "user_id", nullable = false, length = 20)
    private Long userId;

    @Column(name = "office_key", nullable = false, length = 20)
    private String officeKey;

    @Column(name = "referral_code", length = 255, unique = true, nullable = false)
    private String referralCode;

    @PrePersist
    public void prePersist() {
        if (identifier == null) {
            identifier = UUID.randomUUID();
        }
    }
}
