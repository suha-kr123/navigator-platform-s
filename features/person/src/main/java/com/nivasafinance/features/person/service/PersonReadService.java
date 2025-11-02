package com.nivasafinance.features.person.service;

import com.nivasafinance.features.person.dto.PersonResponse;

public interface PersonReadService {
    PersonResponse getPersonById(Long personId);
    PersonResponse getPersonByPrimaryMobile(String mobileNumber);
}

