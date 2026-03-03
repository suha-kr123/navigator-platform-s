package com.nivasafinance.services.authentication.provider.supabase.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupabaseVerifyOtpRequest {
    private String phone;
    private String type;
    private String token;
}
