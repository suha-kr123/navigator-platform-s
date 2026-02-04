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
    implementation(project(":features:person"))
    implementation(project(":features:workflow"))
    implementation(project(":features:advisor"))
    implementation(project(":features:lead"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // Spring Retry for handling optimistic locking conflicts
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
