cpackage com.nivasafinance.features.lead.controller

import com.nivasafinance.features.lead.dto.CreateTaskRequest
import com.nivasafinance.features.lead.dto.CreateTaskResponse
import com.nivasafinance.features.lead.service.LeadTaskService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/leads")
class LeadTaskController(
    private val leadTaskService: LeadTaskService
) {

    @PostMapping("/{leadId}/tasks")
    fun createTaskForLead(
        @PathVariable leadId: UUID,
        @RequestBody request: CreateTaskRequest
    ): ResponseEntity<CreateTaskResponse> {
        return try {
            val response = leadTaskService.createTaskForLead(leadId, request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @GetMapping("/{leadId}/tasks")
    fun getTasksForLead(@PathVariable leadId: UUID): ResponseEntity<List<CreateTaskResponse>> {
        return try {
            val tasks = leadTaskService.getTasksForLead(leadId)
            ResponseEntity.ok(tasks)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
