plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

dependencies {
    implementation(project(":features:document"))
    implementation(project(":redash"))
    implementation(libs.zen.engine)
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}