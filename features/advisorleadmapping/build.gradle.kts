plugins {
    id("kotlin-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":features:advisor"))
    implementation(project(":features:lead"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplicationKt")
}
