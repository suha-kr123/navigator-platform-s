package framework.core.data;

import framework.config.BusinessContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationRestRequest<T> {
    private String url;
    private HttpMethod method;
    private BusinessContext businessContext;
    private ApiContext apiContext;
    private MultiValueMap<String, String> queryParams;
    private T requestBody;
    private MultiValueMap<String, String> headers;
    private boolean isResponseLoggable = true;
    private boolean convertToBase64 = false;

    public URI getUri() {
        return UriComponentsBuilder.fromUriString(this.url)
                .queryParams(queryParams)
                .build()
                .encode()
                .toUri();
    }

    public HttpEntity<T> getHttpEntity() {
        return new HttpEntity<>(this.requestBody, this.headers);
    }
}

