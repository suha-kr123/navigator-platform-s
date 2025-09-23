package com.nivasafinance.features.leadpersons.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.leadpersons.dto.LeadPersonRequest
import com.nivasafinance.features.leadpersons.dto.LeadPersonResponse
import java.util.*

interface LeadPersonsService {

    fun createLeadPerson(leadPersonRequest: LeadPersonRequest): LeadPersonResponse

    fun getLeadPersonById(id: UUID): LeadPersonResponse

    fun getAllLeadPersons(paginationRequest: PaginationRequest): PaginatedResponse<LeadPersonResponse>

    fun getLeadPersonsByLeadId(leadId: UUID): List<LeadPersonResponse>

    fun getLeadPersonsByPersonId(personId: UUID): List<LeadPersonResponse>

    fun updateLeadPerson(id: UUID, leadPersonRequest: LeadPersonRequest): LeadPersonResponse

    fun deleteLeadPerson(id: UUID)
}
