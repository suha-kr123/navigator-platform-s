package framework

import framework.config.ThirdPartyServiceList
import framework.core.exception.ServiceFactoryException
import framework.core.service.ThirdPartyServiceConfigReadService
import org.springframework.stereotype.Component

@Component
class ServiceFactory<H : ThirdPartyHandler>(
    val services: Set<ThirdPartyHandler>,
    val thirdPartyServiceConfigReadService: ThirdPartyServiceConfigReadService
) {
    private val servicesMap: MutableMap<String, ThirdPartyHandler> = HashMap()

    init {
        services.associateByTo(servicesMap) { it.getKey().serviceName }
    }

    fun getHandler(service: ThirdPartyServiceList): H {
        val serviceConfig = thirdPartyServiceConfigReadService.findByService(service)
        val handler = servicesMap[service.serviceName] ?: throw ServiceFactoryException("Error fetching $service")
        handler.setupConfig(serviceConfig)
        @Suppress("UNCHECKED_CAST")
        return handler as H
    }
}
