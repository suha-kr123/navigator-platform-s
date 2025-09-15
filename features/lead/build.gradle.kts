plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}