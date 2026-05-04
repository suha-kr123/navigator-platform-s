plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(project(":common"))
    implementation(project(":features:master"))
    implementation(project(":features:person"))
    implementation(project(":features:usermanagement"))
    implementation(project(":features:workflow"))
    implementation(project(":features:advisor"))
    implementation(project(":features:lead"))
    implementation(project(":features:task"))
    implementation(project(":features:whatsapp"))
    implementation(project(":features:notifications"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
