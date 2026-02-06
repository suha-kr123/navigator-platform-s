plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(libs.poi.ooxml)
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}

