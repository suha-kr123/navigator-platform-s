plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:dataprovider"))
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
    implementation(project(":features:notifications"))
    implementation(project(":features:staff"))
    implementation(project(":features:offices"))
    implementation(project(":features:usermanagement"))
    implementation(project(":features:referral"))
    implementation(project(":features:transaction"))
    implementation(project(":integrations"))
    implementation(project(":features:bre"))
    implementation(project(":features:creditbureau"))
    implementation(project(":features:consent"))
    implementation(project(":redash"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // Spring Retry for handling optimistic locking conflicts
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(project(":features:campaign"))
    implementation(project(":features:bre"))
    testImplementation(libs.mockk)
    implementation(project(":analytics"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}