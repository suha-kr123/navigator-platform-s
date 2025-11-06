package framework.core.repository;

import framework.core.entity.ThirdPartyResponseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThirdPartyResponseLogRepository extends JpaRepository<ThirdPartyResponseLog, UUID> {
}

