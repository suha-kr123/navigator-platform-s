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
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}

