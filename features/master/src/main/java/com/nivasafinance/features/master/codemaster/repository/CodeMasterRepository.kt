package com.nivasafinance.features.master.codemaster.repository

import com.nivasafinance.features.master.codemaster.entity.CodeMaster
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CodeMasterRepository : JpaRepository<CodeMaster, UUID> {

    fun findByCodeName(codeName: String): List<CodeMaster>

    @Query(
        value = "SELECT * FROM code_master WHERE code_name = :codeName AND code_value->>'key' = :key",
        nativeQuery = true
    )
    fun findByCodeNameAndKey(@Param("codeName") codeName: String, @Param("key") key: String): Optional<CodeMaster>
}
