package com.nivasafinance.features.whatsapp.repository;

import com.nivasafinance.features.whatsapp.entity.WhatsappLogLead;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@JaversSpringDataAuditable
public interface WhatsappLogLeadRepository extends JpaRepository<WhatsappLogLead, Long> {

    Page<WhatsappLogLead> findByLeadIdOrderByWhatsappLogIdDesc(Long leadId, Pageable pageable);

    List<WhatsappLogLead> findAllByLeadIdOrderByWhatsappLogIdDesc(Long leadId);
}
