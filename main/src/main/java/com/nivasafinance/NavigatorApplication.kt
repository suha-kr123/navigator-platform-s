package com.nivasafinance

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = [
        "com.nivasafinance.features.notes",
        "com.nivasafinance.features.document",
        "com.nivasafinance.features.identifiers",
        "com.nivasafinance.features.stages",
        "com.nivasafinance.features.stagedefinitions",
        "com.nivasafinance.features.tasks",
        "com.nivasafinance.features.taskdefinitions",
        "com.nivasafinance.features.lead",
        "com.nivasafinance.features.person",
        "com.nivasafinance.features.master",
        "com.nivasafinance.features.address",
        "configs",
        "exception",
        "audit",
        "repository",
        "service",
        "filter"
    ]
)
@EnableJpaRepositories(
    "com.nivasafinance.features.notes",
    "com.nivasafinance.features.document",
    "com.nivasafinance.features.identifiers",
    "com.nivasafinance.features.stages",
    "com.nivasafinance.features.stagedefinitions",
    "com.nivasafinance.features.tasks",
    "com.nivasafinance.features.taskdefinitions",
    "com.nivasafinance.features.lead",
    "com.nivasafinance.features.person",
    "com.nivasafinance.features.master",
    "com.nivasafinance.features.address",
    "repository"
)
@EntityScan(
    "com.nivasafinance.features.notes",
    "com.nivasafinance.features.document",
    "com.nivasafinance.features.identifiers",
    "com.nivasafinance.features.stages",
    "com.nivasafinance.features.stagedefinitions",
    "com.nivasafinance.features.tasks",
    "com.nivasafinance.features.taskdefinitions",
    "com.nivasafinance.features.lead",
    "com.nivasafinance.features.person",
    "com.nivasafinance.features.master",
    "com.nivasafinance.features.address",
    "audit"
)
class NavigatorApplication

fun main(args: Array<String>) {
    runApplication<NavigatorApplication>(args = args)
}
