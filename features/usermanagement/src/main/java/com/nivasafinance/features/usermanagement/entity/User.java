package com.nivasafinance.features.usermanagement.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.javers.core.metamodel.annotation.TypeName;

@Entity
@TypeName("user")
@Table(name = "n_user")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private UserStatus status;
}

