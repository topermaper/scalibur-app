package com.itene.scalibur.config;

public class OsrmConfig {
    public static final String OSRM_BASE_URL = "http://scalibur.itene.com/api/proxy/osrm";
    public final static String ROUTE_URL = OSRM_BASE_URL +  "/route/v1/driving/%s?overview=full"; // Place holder for coordinates
}
