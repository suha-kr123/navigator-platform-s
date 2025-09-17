package framework.core.data

data class RunConfig(
    val primaryConfig: ThirdPartyConfig,
    val fallbackConfig: ThirdPartyConfig? = null,
    val retries: Int = 0
)
