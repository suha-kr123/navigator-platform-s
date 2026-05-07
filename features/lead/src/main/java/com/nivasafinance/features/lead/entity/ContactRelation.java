package com.nivasafinance.features.lead.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.lead.enums.ContactRelationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "n_contact_relation")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ContactRelation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contact_id", nullable = false)
    private Long contactId;

    @Column(name = "related_contact_id", nullable = false)
    private Long relatedContactId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation", nullable = false, length = 100)
    private ContactRelationType relation;

}
