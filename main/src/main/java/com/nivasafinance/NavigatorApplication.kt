package com.nivasafinance

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = [
        "com.nivasafinance.*"
    ]
)
@EnableJpaRepositories(
    "com.nivasafinance.*"
)
@EntityScan(
    "com.nivasafinance.*"
)
class NavigatorApplication

fun main(args: Array<String>) {
    runApplication<NavigatorApplication>(args = args)
}
