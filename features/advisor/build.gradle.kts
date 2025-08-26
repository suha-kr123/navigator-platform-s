plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:person"))
    implementation(project(":features:lead"))
    implementation(project(":features:payment"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}