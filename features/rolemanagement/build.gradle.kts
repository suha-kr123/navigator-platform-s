plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:usermanagement"))
    // Note: Changed to implementation to allow compile-time access to StaffReadService
    implementation(project(":features:staff"))
    implementation(project(":features:offices"))
    implementation(project(":features:person"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}


