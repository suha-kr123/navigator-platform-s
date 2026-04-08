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
    implementation(project(":features:rolemanagement"))
    implementation(project(":features:usermanagement"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockk)
}

tasks.bootJar {
  enabled = false
}
tasks.jar {
  enabled = true
  archiveBaseName.set("atlas-client")
}