package com.nivasafinance.features.income.repository

import com.nivasafinance.features.income.entity.IncomeDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IncomeDetailsRepository : JpaRepository<IncomeDetails, UUID> {
}
