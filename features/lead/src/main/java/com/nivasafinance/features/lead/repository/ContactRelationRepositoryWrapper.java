package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.ContactRelation;
import com.nivasafinance.features.lead.enums.ContactRelationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContactRelationRepositoryWrapper {

    private final ContactRelationRepository contactRelationRepository;

    public ContactRelation saveWithException(ContactRelation contactRelation) {
        return contactRelationRepository.save(contactRelation);
    }

    public List<ContactRelation> findAllByContactId(Long contactId) {
        return contactRelationRepository.findAllByContactId(contactId);
    }

    public boolean exists(Long contactId, Long relatedContactId, ContactRelationType relation) {
        return contactRelationRepository.existsByContactIdAndRelatedContactIdAndRelation(
                contactId, relatedContactId, relation
        );
    }
}
