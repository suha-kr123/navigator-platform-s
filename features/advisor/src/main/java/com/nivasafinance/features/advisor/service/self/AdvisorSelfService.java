package com.nivasafinance.features.advisor.service.self;

import com.nivasafinance.features.advisor.dto.self.SelfAddBankDetailsRequest;
import com.nivasafinance.features.advisor.dto.self.SelfAddBankDetailsResponse;
import com.nivasafinance.features.advisor.dto.self.SelfAddAddressResponse;
import com.nivasafinance.features.advisor.dto.self.SelfAddressRequest;
import com.nivasafinance.features.advisor.dto.self.SelfAddressResponse;
import com.nivasafinance.features.advisor.dto.self.SelfAdvisorProfileRequest;
import com.nivasafinance.features.advisor.dto.self.SelfAdvisorResponse;
import com.nivasafinance.features.advisor.dto.self.SelfBankDetailsRequest;
import com.nivasafinance.features.advisor.dto.self.SelfBankDetailsResponse;

import java.util.List;
import java.util.UUID;

public interface AdvisorSelfService {

    void updateMyProfile(SelfAdvisorProfileRequest request);

    SelfAdvisorResponse getMyProfile();

    SelfAddAddressResponse addMyAddress(SelfAddressRequest request);

    SelfAddressResponse updateMyAddress(String addressId, SelfAddressRequest request);

    List<SelfAddressResponse> getMyAddresses();

    SelfAddressResponse getMyAddress(String addressId);

    SelfAddBankDetailsResponse addMyBankDetails(SelfAddBankDetailsRequest request);

    void updateMyBankDetails(UUID bankIdentifier, SelfBankDetailsRequest request);

    List<SelfBankDetailsResponse> getMyBankDetails();

    SelfBankDetailsResponse getMyBankDetails(UUID bankIdentifier);
}
