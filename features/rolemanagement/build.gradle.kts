plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":security"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}


