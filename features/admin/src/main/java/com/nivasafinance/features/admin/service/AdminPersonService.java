package com.nivasafinance.features.admin.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.person.dto.AdminPersonResponse;

public interface AdminPersonService {
    void deletePerson(String mobileNumber);
    void undoDeletePerson(String mobileNumber);
    PaginatedResponse<AdminPersonResponse> adminSearchPersonsByMobile(PaginationRequest paginationRequest, String mobileNumber);
    PaginatedResponse<AdminPersonResponse> getDeletedPersons(PaginationRequest paginationRequest);
}
