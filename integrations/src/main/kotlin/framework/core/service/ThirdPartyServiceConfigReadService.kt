package framework.core.service

import framework.config.ThirdPartyServiceList
import framework.core.data.RunConfig
import framework.core.exception.ServiceConfigurationException
import framework.core.repository.ThirdPartyServiceConfigRepository
import org.springframework.stereotype.Service

@Service
class ThirdPartyServiceConfigReadService(
    private val thirdPartyServiceConfigRepository: ThirdPartyServiceConfigRepository,
    private val thirdPartyProviderConfigReadService: ThirdPartyProviderConfigReadService
) {

    fun findByService(service: ThirdPartyServiceList): RunConfig {
        val serviceEntity = thirdPartyServiceConfigRepository.findByServiceAndIsActiveTrue(service.serviceName)
            ?: throw ServiceConfigurationException("Error fetching $service")

        val primaryConfig = thirdPartyProviderConfigReadService.getProviderConfigById(serviceEntity.primaryConfigKey)
        val fallbackConfig =
            serviceEntity.fallbackConfigKey?.let { thirdPartyProviderConfigReadService.getProviderConfigById(it) }
        return RunConfig(primaryConfig, fallbackConfig, serviceEntity.retryCount)
    }
}
