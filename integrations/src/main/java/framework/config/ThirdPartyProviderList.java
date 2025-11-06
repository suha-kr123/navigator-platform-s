package framework.config;

public enum ThirdPartyProviderList {
    EXOTEL("exotel");

    private final String provideName;

    ThirdPartyProviderList(String provideName) {
        this.provideName = provideName;
    }

    public String getProvideName() {
        return provideName;
    }
}
