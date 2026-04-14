package com.nivasafinance.features.usermanagement.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.javers.core.metamodel.annotation.TypeName;

import java.util.Map;

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

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", columnDefinition = "jsonb")
    private Map<String, Object> preferences;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}

