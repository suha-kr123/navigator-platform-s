package com.nivasafinance.features.whatsapp.repository;

import com.nivasafinance.features.whatsapp.entity.WhatsappLog;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface WhatsappLogRepository extends JpaRepository<WhatsappLog, Long> {

    Optional<WhatsappLog> findByIdentifier(UUID identifier);

    Optional<WhatsappLog> findByProviderMessageId(String providerMessageId);
}
