plugins {
	idea
	base
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
apply(from = "git-hooks.gradle.kts")
