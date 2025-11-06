package framework.core.entity;

import com.nivasafinance.common.annotations.NoArg;
import com.nivasafinance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.javers.core.metamodel.annotation.TypeName;

import java.util.UUID;

@Entity
@Table(name = "third_party_response_log")
@TypeName("third_party_response_log")
@NoArg
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ThirdPartyResponseLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", length = 3, nullable = false)
    private Integer entityType;

    @Column(name = "entity_id", length = 20, nullable = true)
    private String entityId;

    @Column(name = "request_method", length = 16, nullable = false)
    private String requestMethod;

    @Column(name = "url", length = 512, nullable = false)
    private String url;

    @Column(name = "request")
    private String request;

    @Column(name = "response")
    private String response;

    @Column(name = "http_status_code", length = 4)
    private Integer httpStatusCode;

    @Column(name = "response_time_ms", length = 20)
    private Long responseTimeInMs;

    @Column(name = "provider_name", length = 50)
    private String providerName;

    @Column(name = "provider_config_id", length = 50)
    private String providerRefId;

    @Column(name = "business_purpose")
    private String businessPurpose;

    @Column(name = "business_entity")
    private String businessEntityName;

    @Column(name = "api_purpose")
    private String apiPurpose;
}

