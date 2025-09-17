package framework.core.data

import framework.config.BusinessContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

data class IntegrationRestRequest<T>(
    val url: String,
    val method: HttpMethod,
    val businessContext: BusinessContext,
    val apiContext: ApiContext,
    val queryParams: MultiValueMap<String, String>? = null,
    val requestBody: T? = null,
    val headers: MultiValueMap<String, String>? = null,
    val isResponseLoggable: Boolean = true,
    val convertToBase64: Boolean = false
) {
    fun getUri(): URI {
        return UriComponentsBuilder.fromUriString(this.url).queryParams(queryParams).build().encode().toUri()
    }

    fun getHttpEntity(): HttpEntity<T> {
        return HttpEntity(this.requestBody, this.headers)
    }
}
