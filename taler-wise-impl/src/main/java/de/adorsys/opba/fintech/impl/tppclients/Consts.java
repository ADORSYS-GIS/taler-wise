package de.adorsys.opba.fintech.impl.tppclients;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class Consts {
    public static final String HEADER_X_REQUEST_ID = "X-REQUEST-ID";
    public static final Boolean HEADER_COMPUTE_PSU_IP_ADDRESS = true;

    // Actual values are set in feign request interceptor (FeignConfig.java)
    public static final String COMPUTE_X_TIMESTAMP_UTC = null;
    public static final String COMPUTE_X_REQUEST_SIGNATURE = null;
    public static final String COMPUTE_FINTECH_ID = null;
}
