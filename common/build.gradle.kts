plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(libs.postgresql)
    implementation(libs.liquibase)
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}