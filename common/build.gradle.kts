plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(libs.postgresql)
    implementation(libs.liquibase)
    implementation(libs.auth0.jwt)
    implementation(libs.auth0.jwks)
    implementation(libs.javers.spring.boot.starter.sql)
    implementation(platform("software.amazon.awssdk:bom:2.25.64"))
    implementation("software.amazon.awssdk:sqs")
    implementation(libs.aws.secretsmanager)
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
