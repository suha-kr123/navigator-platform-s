package com.nivasafinance.features.document.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

// @Configuration  // Commented out for local development
class StorageConfiguration {

    @Bean
    fun amazonS3(
        @Value("\${aws.access-key:}") accessKey: String,
        @Value("\${aws.secret-key:}") secretKey: String,
        @Value("\${aws.region:ap-south-1}") region: String
    ): AmazonS3 {
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        return AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(region)
            .build()
    }
}
