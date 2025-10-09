package com.nivasafinance.features.tasks.repository

import com.nivasafinance.features.tasks.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TaskRepository : JpaRepository<Task, UUID>
