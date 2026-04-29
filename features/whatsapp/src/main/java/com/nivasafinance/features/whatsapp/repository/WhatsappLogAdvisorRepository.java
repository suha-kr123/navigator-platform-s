package com.nivasafinance.features.whatsapp.repository;

import com.nivasafinance.features.whatsapp.entity.WhatsappLogAdvisor;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@JaversSpringDataAuditable
public interface WhatsappLogAdvisorRepository extends JpaRepository<WhatsappLogAdvisor, Long> {

    Page<WhatsappLogAdvisor> findByAdvisorIdOrderByWhatsappLogIdDesc(Long advisorId, Pageable pageable);

    List<WhatsappLogAdvisor> findAllByAdvisorIdOrderByWhatsappLogIdDesc(Long advisorId);
}
