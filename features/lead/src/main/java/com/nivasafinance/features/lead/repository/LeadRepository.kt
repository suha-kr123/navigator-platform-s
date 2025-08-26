package com.nivasafinance.features.lead.repository

import com.nivasafinance.features.lead.entity.Lead
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface LeadRepository : JpaRepository<Lead, UUID>
