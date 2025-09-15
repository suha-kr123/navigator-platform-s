package com.nivasafinance.features.notes.repository

import com.nivasafinance.features.notes.entity.Notes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotesRepository : JpaRepository<Notes, UUID>
