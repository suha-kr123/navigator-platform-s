package com.nivasafinance.features.offices.repository

import com.nivasafinance.features.offices.entity.Office
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface OfficeRepository : JpaRepository<Office, UUID>
