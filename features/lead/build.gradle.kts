plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:master"))
//    implementation(project(":features:tasks"))
 //   implementation(project(":features:pipelines"))
    implementation(project(":features:notes"))
    implementation(project(":features:document"))
    implementation(project(":features:person"))
    implementation(project(":features:address"))
    implementation(project(":features:lender"))
    implementation(project(":features:master"))
    implementation(project(":features:marketing"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}