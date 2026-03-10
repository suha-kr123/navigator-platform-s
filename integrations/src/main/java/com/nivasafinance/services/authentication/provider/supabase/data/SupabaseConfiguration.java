package com.nivasafinance.services.authentication.provider.supabase.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupabaseConfiguration {
    private String projectRef;
    private String apiKey;
}
