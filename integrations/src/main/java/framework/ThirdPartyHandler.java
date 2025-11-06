package framework;

import framework.config.ThirdPartyServiceList;
import framework.core.data.RunConfig;

public abstract class ThirdPartyHandler {
    protected RunConfig runConfig;

    public abstract ThirdPartyServiceList getKey();

    public void setupConfig(RunConfig runConfig) {
        this.runConfig = runConfig;
    }
}

