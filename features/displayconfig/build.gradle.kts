plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
