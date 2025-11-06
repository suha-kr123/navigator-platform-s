plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:master"))
    implementation(project(":features:address"))
    implementation("org.apache.commons:commons-lang3:3.18.0")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}