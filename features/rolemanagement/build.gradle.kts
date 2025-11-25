plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:usermanagement"))
    // Note: Removed staff dependency to break circular dependency. Using reflection instead.
    implementation(project(":features:offices"))
    implementation(project(":features:person"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}


