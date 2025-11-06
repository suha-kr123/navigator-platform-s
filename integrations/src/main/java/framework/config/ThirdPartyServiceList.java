package framework.config;

public enum ThirdPartyServiceList {
    VOICE("voice");

    private final String serviceName;

    ThirdPartyServiceList(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
