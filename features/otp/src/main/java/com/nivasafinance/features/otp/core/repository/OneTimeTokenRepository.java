package com.nivasafinance.features.otp.core.repository;

import com.nivasafinance.features.otp.core.entity.OneTimeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneTimeTokenRepository extends JpaRepository<OneTimeToken, Long> {
}
