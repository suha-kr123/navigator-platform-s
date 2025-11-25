plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:task"))
    implementation(project(":features:master"))
    implementation(project(":features:stage"))
    implementation(project(":features:rolemanagement"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}

