package com.nivasafinance.features.leadotp.repository;

import com.nivasafinance.features.leadotp.entity.LeadOneTimeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadOneTimeTokenRepository extends JpaRepository<LeadOneTimeToken, Long> {
}
