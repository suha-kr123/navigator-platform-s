package com.nivasafinance.features.advisor.service.self;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.enums.AddressType;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.dto.self.*;
import com.nivasafinance.features.advisor.exception.AdvisorExceptionFactory;
import com.nivasafinance.features.advisor.repository.AdvisorDashboardWrapper;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorAddressReadService;
import com.nivasafinance.features.advisor.service.AdvisorAddressWriteService;
import com.nivasafinance.features.advisor.service.AdvisorBankDetailsReadService;
import com.nivasafinance.features.advisor.service.AdvisorBankDetailsWriteService;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.authentication.AuthenticationHandler;
import com.nivasafinance.services.authentication.dto.AuthSendOtpRequest;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpRequest;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.UpdateContactNameRequest;
import com.nivasafinance.features.lead.dto.UpdatePropertyDetailsRequest;
import com.nivasafinance.features.lead.exception.LeadNotFoundException;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryReadService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryDisplayResponse;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;

import org.springframework.util.StringUtils;

import java.util.Optional;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdvisorSelfServiceImpl implements AdvisorSelfService {

    private final AdvisorReadService advisorReadService;
    private final AdvisorWriteService advisorWriteService;
    private final AdvisorAddressReadService advisorAddressReadService;
    private final AdvisorAddressWriteService advisorAddressWriteService;
    private final AdvisorBankDetailsReadService advisorBankDetailsReadService;
    private final AdvisorBankDetailsWriteService advisorBankDetailsWriteService;
    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final AdvisorDashboardWrapper advisorDashboardWrapper;
    private final MessageSource messageSource;
    private final ServiceFactory<AuthenticationHandler> authenticationServiceFactory;

    private final LeadReadService leadReadService;
    private final LeadWriteService leadWriteService;
    private final LeadContactWriteService leadContactWriteService;
    private final LeadStageHistoryReadService leadStageHistoryReadService;
    private final ProductReadService productReadService;

    @Override
    public void updateMyProfile(SelfAdvisorProfileRequest request) {
        UUID meId = resolveMeIdentifier();
        AdvisorResponse existing = advisorReadService.getAdvisorByIdentifier(meId);
        UpdateAdvisorRequest updateRequest = buildUpdateAdvisorRequestMerged(request, existing);
        // Only call updateAdvisor when there is something to update; otherwise person data would be overwritten with null.
        // Null/empty personalDetails and mobileNumberDetails = no person update (e.g. client sent only qualification/occupation).
        boolean hasPersonUpdate = updateRequest.getPersonalDetails() != null
                || (updateRequest.getMobileNumberDetails() != null && !updateRequest.getMobileNumberDetails().isEmpty());
        if (hasPersonUpdate) {
            advisorWriteService.updateAdvisor(meId, updateRequest);
        }
        // Null = omitted; only update when client sent qualificationDetails.
        if (request.getQualificationDetails() != null) {
            SelfQualificationDetailsRequest qReq = request.getQualificationDetails().orElse(null);
            if (qReq != null) {
                String value = qReq.getHighestQualification() != null ? qReq.getHighestQualification().orElse(null) : null;
                advisorWriteService.updateQualificationDetails(meId, new UpdateQualificationDetailsRequest(value));
            }
        }
        // Null = omitted; only update when client sent occupationDetails.
        if (request.getOccupationDetails() != null) {
            SelfOccupationDetailsRequest oReq = request.getOccupationDetails().orElse(null);
            if (oReq != null) {
                String type = oReq.getOccupationType() != null ? oReq.getOccupationType().orElse(null) : null;
                String occ = oReq.getOccupation() != null ? oReq.getOccupation().orElse(null) : null;
                advisorWriteService.updateOccupationDetails(meId, new UpdateOccupationDetailsRequest(type, occ));
            }
        }
    }

    @Override
    public SelfAdvisorResponse getMyProfile() {
        UUID meId = resolveMeIdentifier();
        AdvisorResponse full = advisorReadService.getAdvisorByIdentifier(meId);
        List<AddressData> addresses = advisorAddressReadService.getAddresses(meId);
        List<BankDetailsResponse> banks = advisorBankDetailsReadService.getAllBankDetails(meId);
        // Null from service = no addresses/banks; return empty list so response never has null.
        List<SelfAddressResponse> addressDetails = addresses != null
                ? addresses.stream().map(this::fromAddressData).collect(Collectors.toList())
                : Collections.emptyList();
        List<SelfBankDetailsResponse> bankDetails = banks != null
                ? banks.stream().map(this::toSelfBankDetailsResponse).collect(Collectors.toList())
                : Collections.emptyList();
        SelfReferredByDetails referredByDetails = null;
        if (full.getReferredByName() != null || full.getReferredByType() != null) {
            referredByDetails = SelfReferredByDetails.builder()
                    .referredByName(full.getReferredByName())
                    .referredByType(full.getReferredByType())
                    .build();
        }
        return SelfAdvisorResponse.builder()
                .status(full.getStatus())
                .personalDetails(full.getPersonalDetails())
                .qualificationDetails(full.getQualificationDetails())
                .occupationDetails(full.getOtherDetails())
                .segmentationDetails(full.getSegmentationDetails())
                .addressDetails(addressDetails)
                .bankDetails(bankDetails)
                .referredByDetails(referredByDetails)
                .build();
    }

    @Override
    public SelfAddAddressResponse addMyAddress(SelfAddressRequest request) {
        UUID meId = resolveMeIdentifier();
        List<AddressData> addressesBefore = advisorAddressReadService.getAddresses(meId);
        boolean hadNoAddresses = addressesBefore == null || addressesBefore.isEmpty();
        String addressId = advisorAddressWriteService.addAddress(meId, toAddressRequest(request));
        if (hadNoAddresses) {
            advisorWriteService.activateAdvisor(meId);
        }
        return SelfAddAddressResponse.builder()
                .addressIdentifier(addressId)
                .build();
    }

    @Override
    public SelfAddressResponse updateMyAddress(String addressId, SelfAddressRequest request) {
        UUID meId = resolveMeIdentifier();
        AddressData current = advisorAddressReadService.getAddress(meId, addressId);
        AddressRequest merged = toAddressRequestMerged(current, request);
        AddressData data = advisorAddressWriteService.updateAddress(meId, addressId, merged);
        return fromAddressData(data);
    }

    @Override
    public List<SelfAddressResponse> getMyAddresses() {
        List<AddressData> addresses = advisorAddressReadService.getAddresses(resolveMeIdentifier());
        // Null = no addresses; return empty list so response never null.
        return addresses != null ? addresses.stream().map(this::fromAddressData).collect(Collectors.toList()) : Collections.emptyList();
    }

    @Override
    public SelfAddressResponse getMyAddress(String addressId) {
        AddressData data = advisorAddressReadService.getAddress(resolveMeIdentifier(), addressId);
        return fromAddressData(data);
    }

    @Override
    public SelfAddBankDetailsResponse addMyBankDetails(SelfAddBankDetailsRequest request) {
        UUID meId = resolveMeIdentifier();
        AddBankDetailsRequest addRequest = toAddBankDetailsRequest(request);
        UUID bankIdentifier = advisorBankDetailsWriteService.addBankDetails(meId, addRequest);
        return SelfAddBankDetailsResponse.builder()
                .bankDetailsIdentifier(bankIdentifier)
                .build();
    }

    private AddBankDetailsRequest toAddBankDetailsRequest(SelfAddBankDetailsRequest r) {
        // Optional null = omitted; unwrap for validation. Required: nameAsPerPassbook, accountNo, ifscCode (non-null, non-blank).
        String nameAsPerPassbook = r.getNameAsPerPassbook() != null ? r.getNameAsPerPassbook().orElse(null) : null;
        String accountNo = r.getAccountNo() != null ? r.getAccountNo().orElse(null) : null;
        String ifscCode = r.getIfscCode() != null ? r.getIfscCode().orElse(null) : null;
        if (nameAsPerPassbook == null || nameAsPerPassbook.isBlank()
                || accountNo == null || accountNo.isBlank()
                || ifscCode == null || ifscCode.isBlank()) {
            throw AdvisorExceptionFactory.bankDetailsRequiredFieldsMissing(messageSource);
        }
        // Optional fields: null/omitted = not set in request.
        return AddBankDetailsRequest.builder()
                .isPrimary(r.getIsPrimary() != null ? r.getIsPrimary().orElse(null) : null)
                .nameAsPerPassbook(nameAsPerPassbook)
                .accountNo(accountNo)
                .ifscCode(ifscCode)
                .bankName(r.getBankName() != null ? r.getBankName().orElse(null) : null)
                .upid(r.getUpid() != null ? r.getUpid().orElse(null) : null)
                .build();
    }

    @Override
    public void updateMyBankDetails(UUID bankIdentifier, SelfBankDetailsRequest request) {
        UUID meId = resolveMeIdentifier();
        BankDetailsResponse existing = findBankDetailsByIdentifier(meId, bankIdentifier);
        UpdateBankDetailsRequest updateRequest = toUpdateBankDetailsRequest(request, existing);
        advisorBankDetailsWriteService.updateBankDetails(meId, bankIdentifier, updateRequest);
    }

    @Override
    public List<SelfBankDetailsResponse> getMyBankDetails() {
        List<BankDetailsResponse> banks = advisorBankDetailsReadService.getAllBankDetails(resolveMeIdentifier());
        // Null = no bank details; return empty list so response never null.
        return banks != null
                ? banks.stream().map(this::toSelfBankDetailsResponse).collect(Collectors.toList())
                : Collections.emptyList();
    }

    @Override
    public SelfBankDetailsResponse getMyBankDetails(UUID bankIdentifier) {
        BankDetailsResponse bank = findBankDetailsByIdentifier(resolveMeIdentifier(), bankIdentifier);
        return toSelfBankDetailsResponse(bank);
    }

    @Override
    public void sendOtp(SelfSendOtpRequest request) {
        String mobileNo = request.getMobileNo();
        if (mobileNo == null || mobileNo.isBlank()) {
            throw AdvisorExceptionFactory.badRequest(messageSource);
        }
        String username = advisorRepositoryWrapper.findAdvisorByMobileNo(mobileNo.trim())
                .map(AdvisorBasicResponse::getUsername)
                .orElse(mobileNo.trim());
        AuthenticationHandler handler = authenticationServiceFactory.getHandler(ThirdPartyServiceList.AUTHENTICATION);
        handler.sendOtp(
                new AuthSendOtpRequest(mobileNo.trim(), username),
                new BusinessContext("ADVISOR", null, "SEND_OTP"));
    }

    @Override
    public SelfVerifyOtpResponse verifyOtp(SelfVerifyOtpRequest request) {
        String mobileNo = request.getMobileNo();
        String otp = request.getOtp();
        if (mobileNo == null || mobileNo.isBlank() || otp == null || otp.isBlank()) {
            throw AdvisorExceptionFactory.badRequest(messageSource);
        }
        AuthenticationHandler handler = authenticationServiceFactory.getHandler(ThirdPartyServiceList.AUTHENTICATION);
        var authResponse = handler.verifyOtp(
                new AuthVerifyOtpRequest(mobileNo.trim(), otp),
                new BusinessContext("ADVISOR", null, "VERIFY_OTP"));
        Optional<AdvisorBasicResponse> existing = advisorRepositoryWrapper.findAdvisorByMobileNo(mobileNo.trim());
        if (existing.isPresent()) {
            return SelfVerifyOtpResponse.builder()
                    .username(existing.get().getUsername())
                    .accessToken(authResponse.getAccessToken())
                    .refreshToken(authResponse.getRefreshToken())
                    .expiresAt(authResponse.getExpiresAt())
                    .isNewUser(false)
                    .build();
        }
        CreateAdvisorRequest createRequest = new CreateAdvisorRequest(
                new MobileNumberDetails(mobileNo.trim(), true, null),
                null, null, null, null, null);
        advisorWriteService.createAdvisor(createRequest);
        return SelfVerifyOtpResponse.builder()
                .username(mobileNo.trim())
                .accessToken(authResponse.getAccessToken())
                .refreshToken(authResponse.getRefreshToken())
                .expiresAt(authResponse.getExpiresAt())
                .isNewUser(true)
                .build();
    }

    private SelfBankDetailsResponse toSelfBankDetailsResponse(BankDetailsResponse b) {
        if (b == null) {
            return null; // Caller should avoid null; defensive.
        }
        return SelfBankDetailsResponse.builder()
                .bankIdentifier(b.getBankIdentifier())
                .isPrimary(b.getIsPrimary())
                .nameAsPerPassbook(b.getNameAsPerPassbook())
                .accountNo(b.getAccountNo())
                .bankName(b.getBankName())
                .ifscCode(b.getIfscCode())
                .upid(b.getUpid())
                .status(b.getStatus())
                .build();
    }

    private BankDetailsResponse findBankDetailsByIdentifier(UUID meId, UUID bankIdentifier) {
        return advisorBankDetailsReadService.getAllBankDetails(meId).stream()
                .filter(b -> bankIdentifier.equals(b.getBankIdentifier()))
                .findFirst()
                .orElseThrow(() -> AdvisorExceptionFactory.retrieveEntityFailed(messageSource));
    }

    @Override
    public AdvisorSelfLeadCheckResponse checkAdvisorSelfLead(AdvisorSelfLeadCheckRequest request) {
        boolean exists = leadReadService.hasLeadWithMobileNumber(request.getMobileNumber());
        return AdvisorSelfLeadCheckResponse.builder().exists(exists).build();
    }

    @Override
    public AdvisorSelfLeadCreateResponse createAdvisorSelfLead(AdvisorSelfLeadCreateRequest request) {
        UUID meId = resolveMeIdentifier();
        com.nivasafinance.features.advisor.entity.Advisor advisor =
                advisorRepositoryWrapper.findByIdentifierWithException(meId);
        String referralCode = advisor.getReferralCode();
        if (referralCode == null || referralCode.isBlank()) {
            throw AdvisorExceptionFactory.advisorReferralCodeNotAvailable(messageSource);
        }

        CreateLeadRequest createRequest = new CreateLeadRequest();
        createRequest.setRequestedLoanAmount(request.getRequestedAmount());
        createRequest.setProduct(request.getProduct());
        createRequest.setPhoneNumber(new CreateLeadRequest.MobileNumberDetails(
                request.getMobileNumber(),
                false));

        SourcingChannelRequest.MarketingDetails marketingDetails = SourcingChannelRequest.MarketingDetails.builder()
                .referredByCode(referralCode)
                .build();
        createRequest.setSourcingChannelRequest(new SourcingChannelRequest(null, null, marketingDetails));

        CreateLeadResponse response = leadWriteService.createLead(createRequest);
        updateAdvisorSelfLead(response.getLeadIdentifier(), response.getContactIdentifier(), request);

        return AdvisorSelfLeadCreateResponse.builder()
                .leadIdentifier(response.getLeadIdentifier())
                .contactIdentifier(response.getContactIdentifier())
                .build();
    }

    @Override
    public void updateAdvisorSelfLead(UUID leadIdentifier, UUID contactIdentifier, AdvisorSelfLeadCreateRequest request) {
        if (hasNameRequest(request)) {
            UpdateContactNameRequest nameRequest = UpdateContactNameRequest.builder()
                    .firstName(request.getFirstName())
                    .middleName(request.getMiddleName())
                    .lastName(request.getLastName())
                    .build();
            leadContactWriteService.updateContactName(leadIdentifier, contactIdentifier, nameRequest);
        }
        if (hasLocationRequest(request)) {
            AddressRequest addressRequest = new AddressRequest();
            addressRequest.setAddressType(AddressType.CURRENT);
            AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest();
            if (request.getDistrictCode() != null && !request.getDistrictCode().isBlank()) {
                location.setDistrictCode(request.getDistrictCode());
            }
            if (request.getStateCode() != null && !request.getStateCode().isBlank()) {
                location.setStateCode(request.getStateCode());
            }
            if (request.getCountryCode() != null && !request.getCountryCode().isBlank()) {
                location.setCountryCode(request.getCountryCode());
            }
            addressRequest.setLocation(location);
            UpdatePropertyDetailsRequest propRequest = UpdatePropertyDetailsRequest.builder()
                    .address(addressRequest)
                    .build();
            leadWriteService.updatePropertyDetails(leadIdentifier, propRequest);
        }
    }

    @Override
    public PaginatedResponse<AdvisorSelfLeadResponse> getSelfAdvisorLeads(PaginationRequest paginationRequest) {
        UUID meId = resolveMeIdentifier();
        com.nivasafinance.features.advisor.entity.Advisor advisor =
                advisorRepositoryWrapper.findByIdentifierWithException(meId);
        String referralCode = advisor.getReferralCode();
        if (referralCode == null || referralCode.isBlank()) {
            throw AdvisorExceptionFactory.advisorReferralCodeNotAvailable(messageSource);
        }
        PaginatedResponse<LeadBasicResponse> paginated = leadReadService.getLeadsByReferralCode(referralCode, paginationRequest);
        List<AdvisorSelfLeadResponse> content = new ArrayList<>();
        for (LeadBasicResponse basic : paginated.getContent()) {
            content.add(AdvisorSelfLeadResponse.builder()
                    .leadIdentifier(basic.getLeadIdentifier())
                    .leadName(basic.getPrimaryContactName())
                    .leadNumber(basic.getPrimaryContactPhone())
                    .loanType(resolveProductName(basic.getProductCode()))
                    .leadStatus(basic.getStatus())
                    .leadSubStatus(basic.getSubstatus())
                    .requestedAmount(basic.getRequestedAmount())
                    .createdAt(basic.getCreatedAt())
                    .leadStageDisplayName(resolveCurrentStageDisplayName(basic.getLeadIdentifier()))
                    .build());
        }
        return new PaginatedResponse<>(content, paginated.getPagination());
    }

    @Override
    public PaginatedResponse<AdvisorSelfLeadResponse> getSelfAdvisorLeadsWithSearch(
            PaginationRequest paginationRequest, String mobileNumber, String name,
            String status, String subStatus) {
        UUID meId = resolveMeIdentifier();
        com.nivasafinance.features.advisor.entity.Advisor advisor =
                advisorRepositoryWrapper.findByIdentifierWithException(meId);
        String referralCode = advisor.getReferralCode();
        if (referralCode == null || referralCode.isBlank()) {
            throw AdvisorExceptionFactory.advisorReferralCodeNotAvailable(messageSource);
        }
        PaginatedResponse<AdvisorSelfLeadResponse> response = advisorRepositoryWrapper.findLeadsByReferralCodeWithSearch(
                referralCode, paginationRequest, mobileNumber, name, status, subStatus);
        for (AdvisorSelfLeadResponse item : response.getContent()) {
            item.setLoanType(resolveProductName(item.getLoanType()));
            item.setLeadStageDisplayName(resolveCurrentStageDisplayName(item.getLeadIdentifier()));
        }
        return response;
    }

    @Override
    public AdvisorSelfLeadResponse getSelfAdvisorLeadByLeadId(UUID leadIdentifier) {
        UUID meId = resolveMeIdentifier();
        com.nivasafinance.features.advisor.entity.Advisor advisor =
                advisorRepositoryWrapper.findByIdentifierWithException(meId);
        String referralCode = advisor.getReferralCode();
        if (referralCode == null || referralCode.isBlank()) {
            throw AdvisorExceptionFactory.advisorReferralCodeNotAvailable(messageSource);
        }
        LeadResponse lead;
        try {
            lead = leadReadService.getLeadByIdentifier(leadIdentifier);
        } catch (LeadNotFoundException e) {
            throw AdvisorExceptionFactory.selfLeadNotAccessible(messageSource);
        }
        if (lead.getReferredByCode() == null || !lead.getReferredByCode().equals(referralCode)) {
            throw AdvisorExceptionFactory.selfLeadNotAccessible(messageSource);
        }
        return AdvisorSelfLeadResponse.builder()
                .leadIdentifier(lead.getLeadIdentifier())
                .leadName(lead.getPrimaryPersonName())
                .leadNumber(lead.getPrimaryPersonNumber())
                .loanType(resolveProductName(lead.getProductCode()))
                .leadStatus(lead.getStatus())
                .leadSubStatus(lead.getSubStatus())
                .requestedAmount(lead.getRequestedAmount())
                .createdAt(lead.getLeadCreatedAt())
                .leadStageDisplayName(resolveCurrentStageDisplayName(leadIdentifier))
                .build();
    }

    @Override
    public SelfAdvisorDashboardResponse getMyDashboard() {
        UUID meId = resolveMeIdentifier();
        SelfAdvisorDashboard dashboard = advisorDashboardWrapper.getSelfDashboard(meId)
                .orElseThrow(() -> AdvisorExceptionFactory.notFoundForCurrentUser(messageSource));

        return SelfAdvisorDashboardResponse.builder()
                .advisorName(dashboard.getName())
                .salesOwner(dashboard.getSalesOwner())
                .salesOwnerMobile(dashboard.getSalesOwnerMobile())
                .leadCounts(advisorDashboardWrapper.getLeadCountsByStatusForAdvisor(meId))
                .build();
    }

    @Override
    public List<AdvisorSelfLeadStageHistoryResponse> getSelfAdvisorLeadStageHistory(UUID leadIdentifier) {
        UUID meId = resolveMeIdentifier();
        com.nivasafinance.features.advisor.entity.Advisor advisor =
                advisorRepositoryWrapper.findByIdentifierWithException(meId);
        String referralCode = advisor.getReferralCode();
        if (referralCode == null || referralCode.isBlank()) {
            throw AdvisorExceptionFactory.advisorReferralCodeNotAvailable(messageSource);
        }
        LeadResponse lead;
        try {
            lead = leadReadService.getLeadByIdentifier(leadIdentifier);
        } catch (LeadNotFoundException e) {
            throw AdvisorExceptionFactory.selfLeadNotAccessible(messageSource);
        }
        if (lead.getReferredByCode() == null || !lead.getReferredByCode().equals(referralCode)) {
            throw AdvisorExceptionFactory.selfLeadNotAccessible(messageSource);
        }
        List<LeadStageHistoryDisplayResponse> rows =
                leadStageHistoryReadService.getStageHistoryWithDisplayLabelsByLeadId(leadIdentifier);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<AdvisorSelfLeadStageHistoryResponse> segments = new ArrayList<>();
        String prevExternalStage = null;
        LocalDateTime segmentEnteredAt = null;
        LocalDateTime segmentExitedAt = null;

        for (LeadStageHistoryDisplayResponse row : rows) {
            String externalStage = row.getDisplayLabel();
            if (externalStage != null && externalStage.equals(prevExternalStage)) {
                segmentExitedAt = row.getExitedAt();
            } else {
                if (prevExternalStage != null) {
                    segments.add(AdvisorSelfLeadStageHistoryResponse.builder()
                            .externalStage(prevExternalStage)
                            .enteredAt(segmentEnteredAt)
                            .exitedAt(segmentExitedAt)
                            .build());
                }
                prevExternalStage = externalStage;
                segmentEnteredAt = row.getEnteredAt();
                segmentExitedAt = row.getExitedAt();
            }
        }
        if (prevExternalStage != null) {
            segments.add(AdvisorSelfLeadStageHistoryResponse.builder()
                    .externalStage(prevExternalStage)
                    .enteredAt(segmentEnteredAt)
                    .exitedAt(segmentExitedAt)
                    .build());
        }
        return segments;
    }


    private String resolveProductName(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            return productCode;
        }
        try {
            return productReadService.getProductByCode(productCode).getName();
        } catch (Exception e) {
            return productCode;
        }
    }

    private String resolveCurrentStageDisplayName(UUID leadIdentifier) {
        List<LeadStageHistoryDisplayResponse> history =
                leadStageHistoryReadService.getStageHistoryWithDisplayLabelsByLeadId(leadIdentifier);
        if (history == null || history.isEmpty()) {
            return null;
        }
        LeadStageHistoryDisplayResponse latest = history.stream()
                .filter(r -> r.getExitedAt() == null)
                .findFirst()
                .orElse(history.get(history.size() - 1));
        String label = latest.getDisplayLabel();
        return StringUtils.hasText(label) ? label : null;
    }

    private static boolean hasNameRequest(AdvisorSelfLeadCreateRequest request) {
        return (request.getFirstName() != null && !request.getFirstName().isBlank())
                || (request.getMiddleName() != null && !request.getMiddleName().isBlank())
                || (request.getLastName() != null && !request.getLastName().isBlank());
    }

    private static boolean hasLocationRequest(AdvisorSelfLeadCreateRequest request) {
        return (request.getDistrictCode() != null && !request.getDistrictCode().isBlank())
                || (request.getStateCode() != null && !request.getStateCode().isBlank())
                || (request.getCountryCode() != null && !request.getCountryCode().isBlank());
    }

    private UUID resolveMeIdentifier() {
        String username = UserContext.getUsername();
        if (username == null || username.isBlank()) {
            throw AdvisorExceptionFactory.noCurrentUser(messageSource);
        }
        return advisorRepositoryWrapper.findByUsername(username)
                .map(com.nivasafinance.features.advisor.entity.Advisor::getIdentifier)
                .orElseThrow(() -> AdvisorExceptionFactory.notFoundForCurrentUser(messageSource));
    }

    private UpdateAdvisorRequest buildUpdateAdvisorRequestMerged(SelfAdvisorProfileRequest request, AdvisorResponse existing) {
        UpdateAdvisorRequest update = new UpdateAdvisorRequest();
        // personalDetails: null = omitted (don't update). Present with inner null = skip.
        if (request.getPersonalDetails() != null) {
            SelfPersonalDetailsRequest inner = request.getPersonalDetails().orElse(null);
            if (inner != null) {
                // mobileNumbers present but empty = clear not allowed; require at least one.
                if (inner.getMobileNumbers() != null && inner.getMobileNumbers().isPresent() && inner.getMobileNumbers().get().isEmpty()) {
                    throw AdvisorExceptionFactory.personalDetailsMobileNumbersRequired(messageSource);
                }
                PersonalDetails existingPd = existing.getPersonalDetails();
                PersonalDetails pd = new PersonalDetails();
                // Per field: null/omitted = keep existing; present = use request value (may be null to clear).
                pd.setFirstName(inner.getFirstName() != null ? inner.getFirstName().orElse(null) : (existingPd != null ? existingPd.getFirstName() : null));
                pd.setMiddleName(inner.getMiddleName() != null ? inner.getMiddleName().orElse(null) : (existingPd != null ? existingPd.getMiddleName() : null));
                pd.setLastName(inner.getLastName() != null ? inner.getLastName().orElse(null) : (existingPd != null ? existingPd.getLastName() : null));
                pd.setDateOfBirth(inner.getDateOfBirth() != null ? inner.getDateOfBirth().orElse(null) : (existingPd != null ? existingPd.getDateOfBirth() : null));
                pd.setGender(inner.getGender() != null ? inner.getGender().orElse(null) : (existingPd != null ? existingPd.getGender() : null));
                // mobileNumbers: omitted (null or !isPresent) = don't touch, use existing. Present non-empty = use request list.
                if (inner.getMobileNumbers() != null && inner.getMobileNumbers().isPresent() && !inner.getMobileNumbers().get().isEmpty()) {
                    List<com.nivasafinance.features.person.entity.MobileNumberDetails> mobiles = inner.getMobileNumbers()
                            .map(lst -> lst.stream()
                                    .map(m -> com.nivasafinance.features.person.entity.MobileNumberDetails.builder()
                                            .number(m.getMobileNumber())
                                            .isPrimary(m.getIsPrimary())
                                            .isWhatsappAvailable(m.getIsWhatsappAvailable())
                                            .build())
                                    .collect(Collectors.toList()))
                            .orElse(null);
                    pd.setMobileNumbers(mobiles);
                } else {
                    pd.setMobileNumbers(existingPd != null ? existingPd.getMobileNumbers() : null);
                }
                update.setPersonalDetails(pd);
            }
        }
        // mobileNumberDetails: null = omitted. Present = use in update.
        if (request.getMobileNumberDetails() != null) {
            update.setMobileNumberDetails(List.of(request.getMobileNumberDetails()));
        }
        return update;
    }

    private AddressRequest toAddressRequest(SelfAddressRequest r) {
        AddressRequest req = new AddressRequest();
        req.setAddressType(AddressType.CURRENT);
        // Add address: null or !isPresent = omitted; only set when client sent value.
        if (r.getAddress() != null && r.getAddress().isPresent()) {
            req.setAddress(r.getAddress().get());
        }
        if (r.getPincode() != null && r.getPincode().isPresent()) {
            SelfAddressRequest.SelfPincodeRequest p = r.getPincode().get();
            req.setPincode(new AddressRequest.PincodeRequest(
                    p.getPincode() != null ? p.getPincode().orElse(null) : null,
                    p.getVillageCode() != null ? p.getVillageCode().orElse(null) : null,
                    p.getVillageName() != null ? p.getVillageName().orElse(null) : null));
        }
        if (r.getLocation() != null && r.getLocation().isPresent()) {
            SelfAddressRequest.SelfAddressLocationRequest loc = r.getLocation().get();
            // Nested Optional fields: null = omitted; use orElse(null) when building.
            req.setLocation(new AddressRequest.AddressLocationRequest(
                    loc.getDistrictCode() != null ? loc.getDistrictCode().orElse(null) : null,
                    loc.getStateCode() != null ? loc.getStateCode().orElse(null) : null,
                    loc.getCountryCode() != null ? loc.getCountryCode().orElse(null) : null,
                    loc.getTalukaCode() != null ? loc.getTalukaCode().orElse(null) : null,
                    loc.getVillageCode() != null ? loc.getVillageCode().orElse(null) : null,
                    loc.getVillageName() != null ? loc.getVillageName().orElse(null) : null));
        }
        return req;
    }

    private AddressRequest toAddressRequestMerged(AddressData current, SelfAddressRequest request) {
        AddressRequest req = new AddressRequest();
        req.setAddressType(AddressType.CURRENT);
        // address: null/omitted = keep current; present = use request (null to clear).
        req.setAddress(request.getAddress() != null ? request.getAddress().orElse(null) : current.getAddress());
        // pincode: null = omitted = keep current. Present but orElse null = clear. Present with value = use it.
        if (request.getPincode() != null) {
            SelfAddressRequest.SelfPincodeRequest p = request.getPincode().orElse(null);
            if (p != null) {
                req.setPincode(new AddressRequest.PincodeRequest(
                        p.getPincode() != null ? p.getPincode().orElse(null) : null,
                        p.getVillageCode() != null ? p.getVillageCode().orElse(null) : null,
                        p.getVillageName() != null ? p.getVillageName().orElse(null) : null));
            } else {
                req.setPincode(null);
            }
        } else {
            req.setPincode(current.getPincode() != null || current.getVillageCode() != null || current.getVillageName() != null
                    ? new AddressRequest.PincodeRequest(current.getPincode(), current.getVillageCode(), current.getVillageName())
                    : null);
        }
        // location: null = omitted = keep current. Present but orElse null = clear. Present with value = use it.
        if (request.getLocation() != null) {
            SelfAddressRequest.SelfAddressLocationRequest loc = request.getLocation().orElse(null);
            if (loc != null) {
                req.setLocation(new AddressRequest.AddressLocationRequest(
                        loc.getDistrictCode() != null ? loc.getDistrictCode().orElse(null) : null,
                        loc.getStateCode() != null ? loc.getStateCode().orElse(null) : null,
                        loc.getCountryCode() != null ? loc.getCountryCode().orElse(null) : null,
                        loc.getTalukaCode() != null ? loc.getTalukaCode().orElse(null) : null,
                        loc.getVillageCode() != null ? loc.getVillageCode().orElse(null) : null,
                        loc.getVillageName() != null ? loc.getVillageName().orElse(null) : null));
            } else {
                req.setLocation(null);
            }
        } else {
            req.setLocation(current.getDistrictCode() != null || current.getStateCode() != null || current.getCountryCode() != null
                    || current.getTalukaCode() != null || current.getVillageCode() != null || current.getVillageName() != null
                    ? new AddressRequest.AddressLocationRequest(current.getDistrictCode(), current.getStateCode(), current.getCountryCode(),
                    current.getTalukaCode(), current.getVillageCode(), current.getVillageName())
                    : null);
        }
        return req;
    }

    private SelfAddressResponse fromAddressData(AddressData d) {
        return SelfAddressResponse.builder()
                .id(d.getId())
                .addressType(d.getAddressType())
                .address(d.getAddress())
                .pincode(d.getPincode())
                .district(d.getDistrict())
                .taluka(d.getTaluka())
                .districtCode(d.getDistrictCode())
                .talukaCode(d.getTalukaCode())
                .villageCode(d.getVillageCode())
                .villageName(d.getVillageName())
                .isServiceable(d.getIsServiceable())
                .build();
    }

    private UpdateBankDetailsRequest toUpdateBankDetailsRequest(SelfBankDetailsRequest r, BankDetailsResponse existing) {
        // Per field: null/omitted = keep existing (merge). Present = use request value (may be null to clear).
        return UpdateBankDetailsRequest.builder()
                .isPrimary(r.getIsPrimary() != null ? r.getIsPrimary().orElse(null) : existing.getIsPrimary())
                .nameAsPerPassbook(r.getNameAsPerPassbook() != null ? r.getNameAsPerPassbook().orElse(null) : existing.getNameAsPerPassbook())
                .accountNo(r.getAccountNo() != null ? r.getAccountNo().orElse(null) : existing.getAccountNo())
                .ifscCode(r.getIfscCode() != null ? r.getIfscCode().orElse(null) : existing.getIfscCode())
                .bankName(r.getBankName() != null ? r.getBankName().orElse(null) : existing.getBankName())
                .upid(r.getUpid() != null ? r.getUpid().orElse(null) : existing.getUpid())
                .build();
    }
}
