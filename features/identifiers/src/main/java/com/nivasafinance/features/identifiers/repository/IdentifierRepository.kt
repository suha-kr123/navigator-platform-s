package com.nivasafinance.features.identifiers.repository

import com.nivasafinance.features.identifiers.entity.Identifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface IdentifierRepository : JpaRepository<Identifier, UUID> {
}
