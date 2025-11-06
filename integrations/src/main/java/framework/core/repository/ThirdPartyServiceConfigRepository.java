package framework.core.repository;

import framework.core.entity.ThirdPartyServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ThirdPartyServiceConfigRepository extends JpaRepository<ThirdPartyServiceConfig, UUID> {
    Optional<ThirdPartyServiceConfig> findByServiceAndIsActiveTrue(String serviceName);
}

