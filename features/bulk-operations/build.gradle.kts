plugins {
    id("common-feature-conventions")
}

dependencies {

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation(project(":features:lead"))
    implementation(project(":features:workflow"))
    implementation(project(":features:stage"))
    implementation(project(":features:usermanagement"))
    implementation(project(":features:master"))
    implementation(project(":features:rolemanagement"))
    implementation(project(":features:task"))
    implementation(project(":features:document"))
    

    implementation(libs.commons.csv)                                        // commons-csv
    compileOnly("com.google.code.findbugs:findbugs-annotations:3.0.1")      // resolve @SuppressFBWarnings in commons-csv for -Werror
    implementation("org.springframework.retry:spring-retry")                // spring-retry

    implementation(platform("software.amazon.awssdk:bom:2.25.64"))          // AWS SDK BOM
    implementation("software.amazon.awssdk:sqs")                            // AWS SQS             
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
