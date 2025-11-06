package com.nivasafinance.features.master.pincode.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "master_pincode")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Pincode extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;
    
    @Column(name = "area", nullable = false, length = 255)
    private String area;
    
    @Column(name = "district", nullable = true, length = 255)
    private String district;
    
    @Column(name = "state", nullable = true, length = 255)
    private String state;
    
    @Column(name = "country", nullable = true, length = 255)
    private String country;
    
    @Column(name = "is_servicable", nullable = false)
    private Boolean isServicable = false;
}

