package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.ContactRelation;
import com.nivasafinance.features.lead.enums.ContactRelationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRelationRepository extends JpaRepository<ContactRelation, Long> {

    List<ContactRelation> findAllByContactId(Long contactId);

    boolean existsByContactIdAndRelatedContactIdAndRelation(
            Long contactId,
            Long relatedContactId,
            ContactRelationType relation
    );
}
