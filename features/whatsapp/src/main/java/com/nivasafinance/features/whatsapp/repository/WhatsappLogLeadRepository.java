package com.nivasafinance.features.whatsapp.repository;

import com.nivasafinance.features.whatsapp.entity.WhatsappLogLead;
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
public interface WhatsappLogLeadRepository extends JpaRepository<WhatsappLogLead, Long> {

    Page<WhatsappLogLead> findByLeadIdOrderByWhatsappLogIdDesc(Long leadId, Pageable pageable);

    List<WhatsappLogLead> findAllByLeadIdOrderByWhatsappLogIdDesc(Long leadId);

    @Query("""
            SELECT m FROM WhatsappLogLead m, WhatsappLog l
            WHERE m.whatsappLogId = l.id
              AND m.leadId = :leadId
              AND (:createdSource IS NULL OR l.createdSource = :createdSource)
            ORDER BY m.whatsappLogId DESC
            """)
    Page<WhatsappLogLead> findByLeadIdAndCreatedSource(
            @Param("leadId") Long leadId,
            @Param("createdSource") WhatsappCreatedSource createdSource,
            Pageable pageable);

    @Query("""
            SELECT l.createdSource, COUNT(l) FROM WhatsappLogLead m, WhatsappLog l
            WHERE m.whatsappLogId = l.id AND m.leadId = :leadId
            GROUP BY l.createdSource
            """)
    List<Object[]> countByLeadIdGroupByCreatedSource(@Param("leadId") Long leadId);
}
