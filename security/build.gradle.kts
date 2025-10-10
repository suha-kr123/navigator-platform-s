plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    implementation(libs.auth0.jwt)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
}