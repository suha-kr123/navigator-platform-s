plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(libs.postgresql)
    implementation(libs.liquibase)
    implementation(libs.jackson.kotlinmodule)
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}