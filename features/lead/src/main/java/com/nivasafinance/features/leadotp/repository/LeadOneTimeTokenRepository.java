package com.nivasafinance.features.leadotp.repository;

import com.nivasafinance.features.otp.core.enums.OtpStatus;
import com.nivasafinance.features.leadotp.entity.LeadOneTimeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LeadOneTimeTokenRepository extends JpaRepository<LeadOneTimeToken, Long> {
    List<LeadOneTimeToken> findAllByReferenceAndLeadIdAndContactIdAndStatusIn(
            String reference,
            Long leadId,
            Long contactId,
            Collection<OtpStatus> statuses);
}
