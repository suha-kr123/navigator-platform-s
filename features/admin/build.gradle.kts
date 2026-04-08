plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:person"))
    implementation(project(":features:usermanagement"))
    implementation(project(":features:staff"))
    implementation(project(":features:advisor"))
    implementation(project(":features:lead"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
