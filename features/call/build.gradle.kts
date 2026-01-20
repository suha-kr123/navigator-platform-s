plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:usermanagement"))
    implementation(project(":features:person"))
    implementation(project(":integrations"))
    implementation(project(":features:rolemanagement"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}

