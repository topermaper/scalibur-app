package com.itene.scalibur.config;

public final class Config {
    public static final String API_BASE_URL = "http://scalibur.itene.com/api";
    public static final String API_GET_ME =  API_BASE_URL + "/users/me";
    public final static String API_POST_LOGIN = API_BASE_URL + "/users/login";
    public final static String API_GET_ROUTES = API_BASE_URL + "/routes/Kozani?status=RUNNING,READY";
    public final static String API_START_ROUTE = API_BASE_URL + "/routes/%d/start"; // Add route_id placeholder
    public final static String API_END_ROUTE = API_BASE_URL + "/routes/%d/end"; // Add route_id placeholder
    public final static String API_POST_GPS = API_BASE_URL + "/gps/";
    public final static String API_ROUTES = API_BASE_URL + "/routes/"; // + route_id
    public final static String API_POST_ROUTE_CONTAINER = API_BASE_URL + "/routes/%d/container/%d"; // Add route_id, container_id placeholder
    //public final static String API_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE4ODQwNjk5MDgsImlhdCI6MTYyNDg2OTkwOCwic3ViIjoxfQ.TMo6udWhG8RvBcpMHjZZ-6z56_gK50yJvPrF2HFqkaU";
}
