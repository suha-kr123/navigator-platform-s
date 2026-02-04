plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:lead"))
    implementation(project(":integrations"))
    implementation(project(":features:campaign"))
    implementation(project(":features:call"))
    implementation(project(":features:advisor"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
