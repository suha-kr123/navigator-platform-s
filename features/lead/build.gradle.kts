plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:tasks"))
    implementation(project(":features:stages"))
    implementation(project(":features:stagedefinitions"))
    implementation(project(":features:notes"))
    implementation(project(":features:document"))
    implementation(project(":features:person"))
    implementation(project(":features:identifiers"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}