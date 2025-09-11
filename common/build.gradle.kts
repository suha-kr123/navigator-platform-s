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
    implementation(libs.auth0.jwt)
    implementation(libs.auth0.jwks)
    implementation(libs.javers.spring.boot.starter.sql)
    implementation("org.springframework.kafka:spring-kafka")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}
