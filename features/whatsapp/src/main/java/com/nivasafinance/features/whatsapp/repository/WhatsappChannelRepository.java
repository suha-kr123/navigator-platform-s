package com.nivasafinance.features.whatsapp.repository;

import com.nivasafinance.features.whatsapp.entity.WhatsappChannel;
import com.nivasafinance.features.whatsapp.enums.WhatsappChannelStatus;
import com.nivasafinance.features.whatsapp.enums.WhatsappEntity;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface WhatsappChannelRepository extends JpaRepository<WhatsappChannel, Long> {

    Optional<WhatsappChannel> findByChannelId(String channelId);

    List<WhatsappChannel> findByEntityAndStatus(WhatsappEntity entity, WhatsappChannelStatus status);
}
