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
    implementation(project(":features:creditbureau"))
    implementation(project(":features:consent"))
    implementation(project(":integrations"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}