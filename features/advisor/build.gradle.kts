plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:person"))
    implementation(project(":features:marketing"))
    implementation(project(":features:master"))
    implementation(project(":features:notes"))
    implementation(project(":features:offices"))
    implementation(project(":features:staff"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}