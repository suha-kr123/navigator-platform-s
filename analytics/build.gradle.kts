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
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}

