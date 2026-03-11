package com.nivasafinance.services.authentication.provider;

import com.nivasafinance.integrations.framework.ThirdPartyProvider;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.authentication.dto.AuthCreateUserRequest;
import com.nivasafinance.services.authentication.dto.AuthSendOtpRequest;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpRequest;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpResponse;
import com.nivasafinance.services.authentication.provider.supabase.data.SupabaseConfiguration;

public interface AuthenticationProvider extends ThirdPartyProvider<SupabaseConfiguration> {

    void sendOtp(
            AuthSendOtpRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );

    AuthVerifyOtpResponse verifyOtp(
            AuthVerifyOtpRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );

    void createUser(
            AuthCreateUserRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );
}
