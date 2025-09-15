plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:tasks"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}
