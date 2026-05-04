package com.nivasafinance.features.whatsapp.repository;

import com.nivasafinance.features.whatsapp.entity.WhatsappLogAdvisor;
import com.nivasafinance.features.whatsapp.enums.WhatsappCreatedSource;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@JaversSpringDataAuditable
public interface WhatsappLogAdvisorRepository extends JpaRepository<WhatsappLogAdvisor, Long> {

    Page<WhatsappLogAdvisor> findByAdvisorIdOrderByWhatsappLogIdDesc(Long advisorId, Pageable pageable);

    List<WhatsappLogAdvisor> findAllByAdvisorIdOrderByWhatsappLogIdDesc(Long advisorId);

    @Query("""
            SELECT m FROM WhatsappLogAdvisor m, WhatsappLog l
            WHERE m.whatsappLogId = l.id
              AND m.advisorId = :advisorId
              AND l.createdSource = :createdSource
            ORDER BY m.whatsappLogId DESC
            """)
    Page<WhatsappLogAdvisor> findByAdvisorIdAndCreatedSource(
            @Param("advisorId") Long advisorId,
            @Param("createdSource") WhatsappCreatedSource createdSource,
            Pageable pageable);

    @Query("""
            SELECT l.createdSource, COUNT(l) FROM WhatsappLogAdvisor m, WhatsappLog l
            WHERE m.whatsappLogId = l.id AND m.advisorId = :advisorId
            GROUP BY l.createdSource
            """)
    List<Object[]> countByAdvisorIdGroupByCreatedSource(@Param("advisorId") Long advisorId);
}
