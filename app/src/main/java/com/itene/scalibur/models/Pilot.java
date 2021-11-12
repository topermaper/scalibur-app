package com.itene.scalibur.models;

import org.json.JSONException;
import org.json.JSONObject;


public class Pilot {

    private String name;
    private double latitude;
    private double longitude;
    private Integer zoom;

    public Pilot(JSONObject json) {
        try {
            this.name = json.getString("name");
            this.latitude = json.getDouble("center_lat");
            this.longitude = json.getDouble("center_lon");
            this.zoom = json.getInt("zoom_level");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Integer getZoom() {
        return zoom;
    }

    public void setZoom(Integer zoom) {
        this.zoom = zoom;
    }
}