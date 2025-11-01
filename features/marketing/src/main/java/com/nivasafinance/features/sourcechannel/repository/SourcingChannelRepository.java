package com.nivasafinance.features.sourcechannel.repository;

import com.nivasafinance.features.sourcechannel.entity.SourcingChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SourcingChannelRepository extends JpaRepository<SourcingChannel, Long> {
    Optional<SourcingChannel> findBySourcingIdentifier(String sourcingIdentifier);
}
