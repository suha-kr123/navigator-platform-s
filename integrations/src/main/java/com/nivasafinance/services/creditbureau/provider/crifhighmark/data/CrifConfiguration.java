package com.nivasafinance.services.creditbureau.provider.crifhighmark.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "crif")
public class CrifConfiguration {
    private String appId;
    private String merchantId;
    private String userId;
    private String customerId;
    private String productCode;
    private String password;
    private String baseUrl;
}
