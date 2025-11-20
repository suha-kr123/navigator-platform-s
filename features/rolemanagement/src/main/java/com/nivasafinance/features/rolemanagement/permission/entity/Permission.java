package com.nivasafinance.features.rolemanagement.permission.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.rolemanagement.enums.ActionEnum;
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum;
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "n_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Permission extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private ActionEnum action;
    
    @Column(name = "operation")
    @Enumerated(EnumType.STRING)
    private OperationsEnum operation;
    
    @Column(name = "module")
    @Enumerated(EnumType.STRING)
    private ModuleEnum module;
}

