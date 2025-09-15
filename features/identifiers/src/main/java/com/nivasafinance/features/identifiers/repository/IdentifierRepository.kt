package com.nivasafinance.features.identifiers.repository

import com.nivasafinance.features.identifiers.entity.Identifier
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IdentifierRepository : JpaRepository<Identifier, UUID>
