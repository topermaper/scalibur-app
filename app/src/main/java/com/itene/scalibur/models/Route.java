package com.itene.scalibur.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Route {

    private int id;
    private int status;
    private int n_containers;
    private double time_cost;
    private double distance_cost;
    private String pilot;
    private String optimize_by;
    private String slot;
    private Calendar created_at;
    private JSONArray route;

    public Route() {
    }

    public Route(JSONObject json) {
        try {
            this.id = json.getInt("id");
            this.status = json.getInt("status");
            this.n_containers = json.getInt("n_containers");
            this.time_cost = json.getDouble("time_cost");
            this.distance_cost = json.getDouble("distance_cost");
            this.optimize_by = json.getString("optimize_by");
            this.pilot = json.getString("pilot");
            this.slot = json.getString("slot");
            this.route = new JSONArray(json.getString("route"));
            this.created_at = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSz", Locale.getDefault());
            this.created_at.setTime(Objects.requireNonNull(sdf.parse(json.getString("created_at"))));
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getN_containers() {
        return n_containers;
    }

    public void setN_containers(int n_containers) {
        this.n_containers = n_containers;
    }

    public double getTime_cost() {
        return time_cost;
    }

    public void setTime_cost(double time_cost) {
        this.time_cost = time_cost;
    }

    public double getDistance_cost() {
        return distance_cost;
    }

    public void setDistance_cost(double distance_cost) {
        this.distance_cost = distance_cost;
    }

    public String getPilot() {
        return pilot;
    }

    public void setPilot(String pilot) {
        this.pilot = pilot;
    }

    public String getOptimize_by() {
        return optimize_by;
    }

    public void setOptimize_by(String optimize_by) {
        this.optimize_by = optimize_by;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public Calendar getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Calendar created_at) {
        this.created_at = created_at;
    }

    public JSONArray getRoute() {
        return route;
    }

    public void setRoute(JSONArray route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return "Route #" + this.id + " for " + this.slot;
    }
}
