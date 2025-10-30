package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findByIdentifier(UUID identifier);

    List<Contact> findByPersonId(Long personId);

    // TODO: Implement query to find contacts by phoneNo through Person entity
    // This requires joining with Person module which may need native query or service call
    List<Contact> findByPersonIdIn(List<Long> personIds);
}
