plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":integrations"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockk)
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
