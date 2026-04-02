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
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadCheckRequest;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadCheckResponse;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadCreateRequest;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadCreateResponse;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadResponse;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadStageHistoryResponse;
import com.nivasafinance.features.advisor.dto.self.SelfSendOtpRequest;
import com.nivasafinance.features.advisor.dto.self.SelfAdvisorDashboardResponse;
import com.nivasafinance.features.advisor.dto.self.SelfVerifyOtpRequest;
import com.nivasafinance.features.advisor.dto.self.SelfVerifyOtpResponse;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;

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

    void sendOtp(SelfSendOtpRequest request);

    SelfVerifyOtpResponse verifyOtp(SelfVerifyOtpRequest request);

    AdvisorSelfLeadCheckResponse checkAdvisorSelfLead(AdvisorSelfLeadCheckRequest request);

    AdvisorSelfLeadCreateResponse createAdvisorSelfLead(AdvisorSelfLeadCreateRequest request);

    void updateAdvisorSelfLead(UUID leadIdentifier, UUID contactIdentifier, AdvisorSelfLeadCreateRequest request);

    PaginatedResponse<AdvisorSelfLeadResponse> getSelfAdvisorLeads(PaginationRequest paginationRequest);

    PaginatedResponse<AdvisorSelfLeadResponse> getSelfAdvisorLeadsWithSearch(
            PaginationRequest paginationRequest, String mobileNumber, String name,
            String status, String subStatus);

    AdvisorSelfLeadResponse getSelfAdvisorLeadByLeadId(UUID leadIdentifier);

    List<AdvisorSelfLeadStageHistoryResponse> getSelfAdvisorLeadStageHistory(UUID leadIdentifier);

    SelfAdvisorDashboardResponse getMyDashboard();
}
