package framework

import framework.config.ThirdPartyProviderList

interface ThirdPartyProvider<T> {
    fun getKey(): ThirdPartyProviderList
    fun setupConfiguration(map: Map<String, String>): T
}
