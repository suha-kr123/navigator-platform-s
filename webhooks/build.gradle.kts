plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:usermanagement"))
    implementation(project(":features:call"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}

