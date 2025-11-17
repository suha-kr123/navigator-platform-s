package com.nivasafinance.services.whatsapp.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.nivasafinance.services.whatsapp.entity.WhatsAppLog;

import java.util.List;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface WhatsAppLogRepository extends JpaRepository<WhatsAppLog, UUID> {
    
    WhatsAppLog findByMessageId(String messageId);
    
    @Query("SELECT w FROM WhatsAppLog w WHERE w.phoneNumber = :phoneNumber ORDER BY w.createdAt DESC")
    List<WhatsAppLog> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT w FROM WhatsAppLog w WHERE w.status = :status ORDER BY w.createdAt DESC")
    List<WhatsAppLog> findByStatus(@Param("status") String status);
}

