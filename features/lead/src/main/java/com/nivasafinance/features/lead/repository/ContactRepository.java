package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByIdentifier(UUID identifier);

    @Query(value = "SELECT * FROM n_contact WHERE cb_enquiry_id @> CAST(:enquiryId AS jsonb) LIMIT 1", nativeQuery = true)
    Optional<Contact> findByCbEnquiryId(@Param("enquiryId") String enquiryId);
}
