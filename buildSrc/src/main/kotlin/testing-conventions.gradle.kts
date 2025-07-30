import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.kotlin.dsl.*

plugins {
    id("java-conventions")
    jacoco
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

// Testing configuration
tasks.test {
    useJUnitPlatform()
    testLogging {
        events = setOf(FAILED)
        exceptionFormat = FULL
    }
    finalizedBy("jacocoTestReport")
}

// Dependencies
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testImplementation(libs.findLibrary("mockk").get())
    testImplementation(libs.findLibrary("kotlin-test").get())
}

// JaCoCo tool version
jacoco {
    toolVersion = libs.findVersion("jacoco").get().toString()
}

// JaCoCo report configuration
tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.withType<Test>())

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                include("**/service/impl/**")
                exclude(
                    "**/dto/**",
                    "**/entity/**",
                    "**/enum/**",
                    "**/exception/**",
                    "**/config/**"
                )
            }
        })
    )
}
