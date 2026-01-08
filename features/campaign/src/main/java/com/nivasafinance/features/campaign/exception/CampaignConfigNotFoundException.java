package com.nivasafinance.features.campaign.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class CampaignConfigNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = -8123749823749825L;

    public CampaignConfigNotFoundException(String message) {
        super(message);
    }
}

