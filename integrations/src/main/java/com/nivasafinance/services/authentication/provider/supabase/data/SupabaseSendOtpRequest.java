package com.nivasafinance.services.authentication.provider.supabase.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupabaseSendOtpRequest {
    private String phone;
    private Options options;
    private MetaData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        private String channel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaData {
        private String username;
    }
}
