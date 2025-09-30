package com.nivasafinance.features.lender.lenderoffice.repository

import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LenderOfficeRepository : JpaRepository<LenderOffice, UUID> {
    fun findByKey(key: String): LenderOffice?
    fun findByLenderKey(lenderKey: String): List<LenderOffice>
    fun findByAddressId(addressId: UUID): List<LenderOffice>
    fun findByStatus(status: LenderOfficeStatus): List<LenderOffice>

    @Query("SELECT lo FROM LenderOffice lo WHERE lo.lenderKey = :lenderKey AND lo.status = :status")
    fun findByLenderKeyAndStatus(
        @Param("lenderKey") lenderKey: String,
        @Param("status") status: LenderOfficeStatus
    ): List<LenderOffice>
}
