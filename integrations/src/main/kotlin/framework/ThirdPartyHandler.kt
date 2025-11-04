package framework

import framework.config.ThirdPartyServiceList
import framework.core.data.RunConfig

abstract class ThirdPartyHandler {
    lateinit var runConfig: RunConfig
    abstract fun getKey(): ThirdPartyServiceList
    fun setupConfig(runConfig: RunConfig) {
        this.runConfig = runConfig
    }
}
