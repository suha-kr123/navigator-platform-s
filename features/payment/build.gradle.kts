plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}

