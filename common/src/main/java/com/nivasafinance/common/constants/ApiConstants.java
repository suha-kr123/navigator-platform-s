package com.nivasafinance.common.constants;

public class ApiConstants {

    public static final String API_ROOT = "/api";
    public static final String API_VERSION_V1 = "/v1";
    public static final String V1 = API_ROOT + API_VERSION_V1;
    public static final String EXTERNAL_ROOT = "/external";
    public static final String EXTERNAL_V1 = EXTERNAL_ROOT + API_VERSION_V1;
    /** Base path for Atlas inbound callbacks under {@link #EXTERNAL_V1} (e.g. {@code /external/v1/atlas}). */
    public static final String ATLAS_V1 = EXTERNAL_V1 + "/atlas";
    public static final String OPEN_ROOT = "/open";
    public static final String OPEN_API_V1 = OPEN_ROOT + API_ROOT + API_VERSION_V1;;
    public static final String SELF_ROOT = "/self";
    public static final String SELF_V1 = SELF_ROOT + API_VERSION_V1;
}
