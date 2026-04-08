plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:lead"))
    implementation(project(":features:master"))
    implementation(project(":features:person"))
    implementation(project(":integrations"))
    implementation(project(":analytics"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}

tasks.jar {
    archiveBaseName.set("customer-externals")
}
