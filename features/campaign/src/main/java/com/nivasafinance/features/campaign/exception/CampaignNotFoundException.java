package com.nivasafinance.features.campaign.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class CampaignNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = -8123749823749824L;

    public CampaignNotFoundException(String message) {
        super(message);
    }
}

