package com.nivasafinance.features.advisoractivity.service;

import com.nivasafinance.features.advisoractivity.dto.CreateAdvisorActivityRequest;
import com.nivasafinance.features.advisoractivity.dto.CreateAdvisorActivityResponse;

public interface AdvisorActivityWriteService {
    CreateAdvisorActivityResponse createAdvisorActivity(CreateAdvisorActivityRequest request);
}

