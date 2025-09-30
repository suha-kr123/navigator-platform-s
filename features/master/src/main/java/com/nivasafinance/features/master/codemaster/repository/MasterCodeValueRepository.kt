package com.nivasafinance.features.master.codemaster.repository

import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface MasterCodeValueRepository : JpaRepository<MasterCodeValue, UUID> {
    fun findByCodeKey(codeKey: String): List<MasterCodeValue>
    fun findByKeyAndCodeKey(key: String, codeKey: String): Optional<MasterCodeValue>
    fun findByCodeKeyAndIsActiveTrue(codeKey: String): List<MasterCodeValue>
}
