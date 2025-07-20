plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(libs.postgresql)
    implementation(libs.liquibase)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}