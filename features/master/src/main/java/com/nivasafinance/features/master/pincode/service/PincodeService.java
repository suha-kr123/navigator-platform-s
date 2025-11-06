package com.nivasafinance.features.master.pincode.service;

import com.nivasafinance.features.master.pincode.dto.PincodeResponse;

public interface PincodeService {
    PincodeResponse getPincodeDetails(String pincode);
}

