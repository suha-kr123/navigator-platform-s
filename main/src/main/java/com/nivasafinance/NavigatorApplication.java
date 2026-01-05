package com.nivasafinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
    scanBasePackages = {
        "com.nivasafinance.*"
    }
)
@EnableJpaRepositories(
    "com.nivasafinance.*"
)
@EntityScan(
    "com.nivasafinance.*"
)
@EnableFeignClients(basePackages = {"com.nivasafinance.*"})
public class NavigatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NavigatorApplication.class, args);
    }
}

