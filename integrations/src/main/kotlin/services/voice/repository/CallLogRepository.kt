package services.voice.repository

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import services.voice.entity.CallLog
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface CallLogRepository : JpaRepository<CallLog, UUID> {

    fun findByCallSid(callSid: String): CallLog?

    @Query(
        "SELECT c FROM CallLog c WHERE c.entityName = :entityName AND c.entityId = :entityId ORDER BY c.createdAt DESC"
    )
    fun findByEntity(@Param("entityName") entityName: String, @Param("entityId") entityId: Long): List<CallLog>

    @Query(
        "SELECT c FROM CallLog c WHERE c.fromNumber = :fromNumber OR c.toNumber = :toNumber ORDER BY c.createdAt DESC"
    )
    fun findByPhoneNumber(@Param("fromNumber") fromNumber: String, @Param("toNumber") toNumber: String): List<CallLog>

    @Query("SELECT c FROM CallLog c WHERE c.status = :status ORDER BY c.createdAt DESC")
    fun findByStatus(@Param("status") status: String): List<CallLog>

    @Query("SELECT c FROM CallLog c WHERE c.direction = :direction ORDER BY c.createdAt DESC")
    fun findByDirection(@Param("direction") direction: String): List<CallLog>
}
