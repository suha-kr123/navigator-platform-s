plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}

// Force protobuf version to fix CVE-2024-7254
configurations.all {
    resolutionStrategy {
        force(libs.protobuf.java.get())
    }
}

dependencies {
    implementation(project(":features:master"))

    // Cloud storage dependencies - using centralized version management
    implementation(libs.aws.s3)

    // JSON processing - using centralized version management
    // Removed jackson.kotlinmodule as project is now Java-only

    // Explicit protobuf dependency to fix CVE-2024-7254
    implementation(libs.protobuf.java)
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
