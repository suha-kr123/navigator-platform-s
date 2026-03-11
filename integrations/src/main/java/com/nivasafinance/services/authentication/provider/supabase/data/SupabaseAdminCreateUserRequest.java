package com.nivasafinance.services.authentication.provider.supabase.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupabaseAdminCreateUserRequest {
    private String phone;
    private String email;
    private String password;
    @JsonProperty("phone_confirm")
    private Boolean phoneConfirm;
    @JsonProperty("email_confirm")
    private Boolean emailConfirm;
    @JsonProperty("user_metadata")
    private Map<String, Object> userMetadata;
}
