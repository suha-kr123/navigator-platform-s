package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.common.enums.ReferredByType;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorResponse {

    private Long id;
    private UUID identifier;
    private AdvisorStatus status;
    private PersonalDetails personalDetails;
    private AdvisorRemarks remarks;
    private QualificationDetails qualificationDetails;
    private OtherDetails otherDetails;
    private SegmentationDetails segmentationDetails;
    private String ownerUsername;
    private String officeKey;
    private String officeName;
    private String referralCode;
    @Deprecated
    private SourcingChannelResponse sourcingChannelDetails;
    private List<Advisor.SourcingEntry> sourcingHistory;

    //referral details
    private String referredByCode;
    private UUID referredByIdentifier;
    private ReferredByType referredByType;
    private String referredByName;
    private String referredByNumber;
    
}

