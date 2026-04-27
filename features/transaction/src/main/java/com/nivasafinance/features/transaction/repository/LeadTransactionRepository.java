package com.nivasafinance.features.transaction.repository;

import com.nivasafinance.features.transaction.entity.LeadTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadTransactionRepository extends JpaRepository<LeadTransaction, Long> {

    List<LeadTransaction> findByLeadId(Long leadId);

    Optional<LeadTransaction> findByTransactionId(Long transactionId);

    List<LeadTransaction> findByReferralCode(String referralCode);
}
