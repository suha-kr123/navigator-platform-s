package com.nivasafinance.features.lender.lender.repository

import com.nivasafinance.features.lender.lender.entity.Lender
import com.nivasafinance.features.lender.lender.enum.LenderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LenderRepository : JpaRepository<Lender, UUID> {
    fun findByKey(key: String?): Lender?
    fun findByName(name: String): Lender?
    fun findByStatus(status: LenderStatus): List<Lender>
}
