plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:usermanagement"))
    implementation(project(":features:person"))
    implementation(project(":features:offices"))
    implementation(project(":features:rolemanagement"))
    implementation(project(":features:referral"))
    implementation(project(":integrations"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}

