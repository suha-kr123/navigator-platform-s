plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(project(":features:task"))
    implementation(project(":features:person"))
    implementation(project(":features:marketing"))
    implementation(project(":features:master"))
    implementation(project(":features:notes"))
    implementation(project(":features:offices"))
    implementation(project(":features:staff"))
    implementation(project(":features:call"))
    implementation(project(":features:usermanagement"))
    implementation(project(":features:referral"))
    implementation(project(":features:lead"))
    implementation(project(":features:rolemanagement"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}