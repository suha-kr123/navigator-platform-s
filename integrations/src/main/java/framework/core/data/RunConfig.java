package framework.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunConfig {
    private ThirdPartyConfig primaryConfig;
    private ThirdPartyConfig fallbackConfig;
    private int retries = 0;
}

