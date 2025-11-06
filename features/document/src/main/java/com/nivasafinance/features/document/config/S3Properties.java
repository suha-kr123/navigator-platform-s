package com.nivasafinance.features.document.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3Properties {
    private String region = "";
    private String bucketName = "";
    private Boolean useIamRoles = false;
    private String accessKeyId = "";
    private String secretAccessKey = "";
}

