plugins {
	idea
	id("kotlin-conventions")
	id("testing-conventions")
	id("dokka-conventions")
	id("spring-conventions")
}

idea {
	module.isDownloadJavadoc = true
	module.isDownloadSources = true
}

tasks {
	wrapper {
		distributionType = Wrapper.DistributionType.ALL
	}
}

dependencies {
	implementation(project(":common"))
	implementation(project(":features:creditbureau"))
	implementation(project(":features:person"))
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
	destinationDirectory.set(file("${layout.buildDirectory}/classes/java/main"))
}