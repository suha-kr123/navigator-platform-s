package com.nivasafinance.features.person.service;

import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;

public interface PersonWriteService {
    PersonCreateResponse createPerson(PersonCreateRequest personRequest);
    void updatePerson(Long personId, PersonUpdateRequest personUpdateRequest);
}

