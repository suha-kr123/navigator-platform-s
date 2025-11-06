package services.voice.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import services.voice.entity.CallLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface CallLogRepository extends JpaRepository<CallLog, UUID> {

    Optional<CallLog> findByCallSid(String callSid);

    @Query("SELECT c FROM CallLog c WHERE c.entityName = :entityName AND c.entityId = :entityId ORDER BY c.createdAt DESC")
    List<CallLog> findByEntity(@Param("entityName") String entityName, @Param("entityId") Long entityId);

    @Query("SELECT c FROM CallLog c WHERE c.fromNumber = :fromNumber OR c.toNumber = :toNumber ORDER BY c.createdAt DESC")
    List<CallLog> findByPhoneNumber(@Param("fromNumber") String fromNumber, @Param("toNumber") String toNumber);

    @Query("SELECT c FROM CallLog c WHERE c.status = :status ORDER BY c.createdAt DESC")
    List<CallLog> findByStatus(@Param("status") String status);

    @Query("SELECT c FROM CallLog c WHERE c.direction = :direction ORDER BY c.createdAt DESC")
    List<CallLog> findByDirection(@Param("direction") String direction);
}

