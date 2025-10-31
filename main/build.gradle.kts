plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}


dependencies {
    implementation(project(":common"))
    implementation(project(":security"))
    implementation(project(":features:creditbureau"))
    implementation(project(":features:person"))
    implementation(project(":features:advisor"))
    implementation(project(":features:master"))
    implementation(project(":features:address"))
    implementation(project(":features:lead"))
    implementation(project(":features:document"))
    implementation(project(":features:offices"))
    implementation(project(":features:identifiers"))
    implementation(project(":features:notes"))
    implementation(project(":features:notification-executor"))
    implementation(project(":features:marketing"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}
