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
    implementation(project(":features:usermanagement"))
    implementation(project(":features:person"))
    implementation("org.apache.commons:commons-lang3:3.18.0")
    // WebSocket dependency needed for integrations module classes that use WebSocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    // Redis dependency needed for CallNotificationRedisRepository
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}