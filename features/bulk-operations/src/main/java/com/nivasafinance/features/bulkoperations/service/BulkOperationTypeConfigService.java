package com.nivasafinance.features.bulkoperations.service;

import com.nivasafinance.features.bulkoperations.common.dto.BulkOperationTypeResponse;
import java.util.List;

public interface BulkOperationTypeConfigService {

    List<BulkOperationTypeResponse> getAllOperationTypes();
}
