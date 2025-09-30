package com.nivasafinance.features.master.codemaster.repository

import com.nivasafinance.features.master.codemaster.entity.MasterCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface MasterCodeRepository : JpaRepository<MasterCode, UUID> {
    fun findByKey(key: String): Optional<MasterCode>
    fun findByParentId(parentId: UUID): List<MasterCode>
}
