plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("kotlin-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(libs.poi.ooxml)
    implementation("com.posthog:posthog-server:2.3.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}

