package com.nivasafinance.features.bre.service;

import com.nivasafinance.features.bre.dto.BREConfigDetailedResponse;
import com.nivasafinance.features.bre.dto.BREConfigRuleFileUploadResponse;
import com.nivasafinance.features.bre.dto.CreateBREConfigRequest;
import org.springframework.web.multipart.MultipartFile;

public interface BREConfigWriteService {
    BREConfigDetailedResponse createBREConfig(CreateBREConfigRequest request);

    BREConfigRuleFileUploadResponse uploadRuleFile(String uname, MultipartFile file);
}
