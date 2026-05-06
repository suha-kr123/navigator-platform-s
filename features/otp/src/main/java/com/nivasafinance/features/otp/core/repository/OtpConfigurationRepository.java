package com.nivasafinance.features.otp.core.repository;

import com.nivasafinance.features.otp.core.entity.OtpConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpConfigurationRepository extends JpaRepository<OtpConfiguration, Long> {
    Optional<OtpConfiguration> findByUname(String uname);
}
