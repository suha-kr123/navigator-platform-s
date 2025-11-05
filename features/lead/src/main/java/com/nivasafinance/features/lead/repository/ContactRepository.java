package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByIdentifier(UUID identifier);
}
