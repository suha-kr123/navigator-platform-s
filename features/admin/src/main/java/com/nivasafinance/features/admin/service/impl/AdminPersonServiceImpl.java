package com.nivasafinance.features.admin.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.admin.service.AdminPersonService;
import com.nivasafinance.features.person.dto.AdminPersonResponse;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.service.PersonWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPersonServiceImpl implements AdminPersonService {

    private final PersonWriteService personWriteService;
    private final PersonReadService personReadService;

    @Override
    public void deletePerson(String mobileNumber) {
        personWriteService.deletePerson(mobileNumber);
    }

    @Override
    public void undoDeletePerson(String mobileNumber) {
        personWriteService.undoDeletePerson(mobileNumber);
    }

    @Override
    public PaginatedResponse<AdminPersonResponse> adminSearchPersonsByMobile(PaginationRequest paginationRequest, String mobileNumber) {
        return personReadService.adminSearchPersonsByMobile(paginationRequest, mobileNumber);
    }

    @Override
    public PaginatedResponse<AdminPersonResponse> getDeletedPersons(PaginationRequest paginationRequest) {
        return personReadService.getDeletedPersons(paginationRequest);
    }
}
