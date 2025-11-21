plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:address"))
    implementation(project(":features:identifier"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}