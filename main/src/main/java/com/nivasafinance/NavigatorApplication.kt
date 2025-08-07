package com.nivasafinance

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = [
        "com.nivasafinance.features.*",
        "configs",
        "exception",
        "audit",
        "repository",
        "service",
        "filter"
    ]
)
@EnableJpaRepositories("com.nivasafinance.features.*", "repository")
@EntityScan("com.nivasafinance.features.*", "audit")
class NavigatorApplication

fun main(args: Array<String>) {
    runApplication<NavigatorApplication>(args = args)
}
