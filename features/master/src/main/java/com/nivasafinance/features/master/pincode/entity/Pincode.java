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

@Entity
@Table(name = "n_master_pincode")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Pincode extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;
    
    // Foreign key references to location master tables (only IDs stored)
    @Column(name = "country_id", nullable = true)
    private Long countryId;
    
    @Column(name = "state_id", nullable = true)
    private Long stateId;

    @Column(name = "region_id", nullable = true)
    private Long regionId;

    @Column(name = "operating_area_id", nullable = true)
    private Long operatingAreaId;

    @Column(name = "district_id", nullable = true)
    private Long districtId;
    
    @Column(name = "taluka_id", nullable = true)
    private Long talukaId;
    
    @Column(name = "is_servicable", nullable = false)
    private Boolean isServicable = false;
}

