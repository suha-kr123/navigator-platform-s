package com.nivasafinance

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.nivasafinance.features.*", "configs"])
class NavigatorApplication

fun main(args: Array<String>) {
    runApplication<NavigatorApplication>(args = args)
}
