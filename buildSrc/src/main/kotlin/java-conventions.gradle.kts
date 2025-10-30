plugins {
    java
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

java {
    // Auto JDK setup
    toolchain {
        languageVersion.set(
            JavaLanguageVersion.of(libs.findVersion("jdk").get().toString())
        )
    }
}

tasks.compileJava {
    // See: https://docs.oracle.com/en/java/javase/12/tools/javac.html
    @Suppress("SpellCheckingInspection")
    // Enable all warnings except processing warnings
    // Processing warnings occur for runtime annotations (Spring, JPA, Hibernate) that don't need processors
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:all,-processing", // Enables all recommended warnings except processing warnings
            "-Werror", // Terminates compilation when warnings occur.
        )
    )
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
            )
        )
    }
}

repositories {
    mavenCentral()
}

dependencies{
    // add modelmapper
    implementation("org.modelmapper:modelmapper:3.0.0")
    // add lombok
    compileOnly(libs.findLibrary("lombok").get())
    annotationProcessor(libs.findLibrary("lombok").get())
}
