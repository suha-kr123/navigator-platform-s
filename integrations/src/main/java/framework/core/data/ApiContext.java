package framework.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiContext {
    private String providerName;
    private String providerConfigId;
    private String apiPurpose;
}

