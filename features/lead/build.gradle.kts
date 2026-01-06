plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:master"))
    implementation(project(":features:notes"))
    implementation(project(":features:document"))
    implementation(project(":features:person"))
    implementation(project(":features:address"))
    implementation(project(":features:lender"))
    implementation(project(":features:master"))
    implementation(project(":features:marketing"))
    implementation(project(":features:rolemanagement"))
    implementation(project(":features:workflow"))
    implementation(project(":features:task"))
    implementation(project(":features:call"))
    implementation(project(":features:staff"))
    implementation(project(":features:offices"))
    implementation(project(":features:usermanagement"))
    implementation(project(":features:advisor"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(project(":features:campaign"))
    testImplementation(libs.mockk)
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}