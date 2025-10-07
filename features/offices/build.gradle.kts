plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:master"))
    implementation(project(":features:address"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}


