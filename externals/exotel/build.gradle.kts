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
    implementation(project(":features:task"))
    implementation(project(":features:person"))
    implementation(project(":features:workflow"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
