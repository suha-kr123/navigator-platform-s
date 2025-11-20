package com.nivasafinance.integrations.framework;

import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.data.RunConfig;

public abstract class ThirdPartyHandler {
    protected RunConfig runConfig;

    public abstract ThirdPartyServiceList getKey();

    protected void setupConfig(RunConfig runConfig) {
        this.runConfig = runConfig;
    }
}

