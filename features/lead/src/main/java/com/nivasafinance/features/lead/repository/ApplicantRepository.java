package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    Optional<Applicant> findByIdentifier(UUID identifier);

    List<Applicant> findByPersonId(Long personId);

    @Query("SELECT a FROM Applicant a JOIN Person p ON a.personId = p.id " +
           "WHERE EXISTS (SELECT 1 FROM MobileNumberDetails m WHERE m.person.id = p.id AND m.number = :phoneNo)")
    List<Applicant> findByPhoneNo(@Param("phoneNo") String phoneNo);
}
