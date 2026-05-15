package com.nivasafinance.features.leadotp.repository;

import com.nivasafinance.features.otp.core.enums.OtpStatus;
import com.nivasafinance.features.leadotp.entity.LeadOneTimeToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadOneTimeTokenRepository extends JpaRepository<LeadOneTimeToken, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE LeadOneTimeToken token
            SET token.status = :newStatus,
                token.updatedAt = CURRENT_TIMESTAMP
            WHERE token.reference = :reference
              AND token.leadId = :leadId
              AND token.contactId = :contactId
              AND token.status IN :currentStatuses
            """)
    int invalidateActiveTokens(
            @Param("reference") String reference,
            @Param("leadId") Long leadId,
            @Param("contactId") Long contactId,
            @Param("newStatus") OtpStatus newStatus,
            @Param("currentStatuses") Iterable<OtpStatus> currentStatuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE LeadOneTimeToken token
            SET token.status = :status,
                token.updatedAt = CURRENT_TIMESTAMP
            WHERE token.id = :id
            """)
    int updateStatus(@Param("id") Long id, @Param("status") OtpStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE LeadOneTimeToken token
            SET token.leadId = :leadId,
                token.updatedAt = CURRENT_TIMESTAMP
            WHERE token.id = :id
            """)
    int updateLeadId(@Param("id") Long id, @Param("leadId") Long leadId);
}
