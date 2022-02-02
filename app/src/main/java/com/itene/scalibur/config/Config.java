package com.itene.scalibur.config;

public final class Config {
    public static final String API_BASE_URL = "http://scalibur.itene.com";
    public static final String API_GET_ME =  API_BASE_URL + "/api/users/me";
    public final static String API_POST_LOGIN = API_BASE_URL + "/api/users/login";
    public final static String API_GET_ROUTES = API_BASE_URL + "/api/routes/Kozani";
    public final static String API_START_ROUTE = API_BASE_URL + "/api/routes/%d/start"; // Add route_id placeholder
    public final static String API_END_ROUTE = API_BASE_URL + "/api/routes/%d/end"; // Add route_id placeholder
    public final static String API_POST_GPS = API_BASE_URL + "/api/gps/";
    public final static String API_ROUTES = API_BASE_URL + "/api/routes/"; // + route_id
    public final static String API_POST_ROUTE_CONTAINER = API_BASE_URL + "/api/routes/%d/container/%d"; // Add route_id, container_id placeholder
}
