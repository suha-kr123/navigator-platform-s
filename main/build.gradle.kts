plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}


dependencies {
    implementation(project(":common"))
    implementation(project(":features:creditbureau"))
    implementation(project(":features:person"))
}
