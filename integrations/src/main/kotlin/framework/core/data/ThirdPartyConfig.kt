package framework.core.data

import java.util.UUID

data class ThirdPartyConfig(
    val id: UUID,
    val name: String,
    val provider: String,
    val configurations: Map<String, String>,
)
