package com.itene.scalibur.models;

import com.google.android.gms.maps.model.Marker;
import com.itene.scalibur.R;

import org.json.JSONException;
import org.json.JSONObject;


public class Location {

    protected String name;
    private double latitude;
    private double longitude;
    protected Marker marker;

    public Location(JSONObject json) {
        try {
            this.name = json.getString("name");
            this.latitude = json.getDouble("latitude");
            this.longitude = json.getDouble("longitude");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Location(android.location.Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
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

    public double getLongitude() {
        return longitude;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

}
