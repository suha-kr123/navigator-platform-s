package framework.core.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import framework.core.data.ThirdPartyConfig
import framework.core.exception.ServiceConfigurationException
import framework.core.repository.ThirdPartyProviderConfigRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ThirdPartyProviderConfigReadService(val thirdPartyProviderConfigRepository: ThirdPartyProviderConfigRepository) {

    fun getProviderConfigById(id: UUID): ThirdPartyConfig {
        val configEntity = thirdPartyProviderConfigRepository.findById(id).orElseThrow()
        if (!configEntity.active) {
            throw ServiceConfigurationException("Config $id is not active")
        }
        val configurations = Gson().fromJson<MutableMap<String, String>>(
            configEntity.configs,
            object : TypeToken<HashMap<String, String>>() {}.type
        ); // todo
        return ThirdPartyConfig(
            id = configEntity.id,
            provider = configEntity.provider,
            name = configEntity.name,
            configurations = configurations
        )
    }
}
