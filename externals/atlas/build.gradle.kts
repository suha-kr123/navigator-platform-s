plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:call"))
    implementation(project(":features:atlas"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
