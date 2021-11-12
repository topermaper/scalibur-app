package com.itene.scalibur.models;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class Route {

    private int id;
    private StatusEnum status;
    private int n_containers;
    private double time_cost;
    private double distance_cost;
    private Pilot pilot;
    private String optimize_by;
    private String slot;
    private Calendar created_at;
    private ArrayList<Waypoint> waypoints;
    private Location last_known_location;
    //private StatusEnum driving_status;
    private DrivingPath current_path;
    private Waypoint start_depot;
    private Waypoint end_depot;
    private Context context;

    public enum StatusEnum
    {
        READY, RUNNING, PAUSED, FINISHED
    }

    public Route(Context context) {
        this.context = context;
        //this.driving_status = StatusEnum.READY;
    }

    public Route(Context context, Integer route_id) {
        this(context);
        this.id = route_id;
    }

    public Route(Context context, JSONObject json) {
        this(context);
        this.populate(json);
    }

    public void populate(JSONObject json) {
        try {
            this.id = json.getInt("id");
            this.status = StatusEnum.valueOf(json.getString("status"));
            this.n_containers = json.getInt("n_containers");
            this.time_cost = json.getDouble("time_cost");
            this.distance_cost = json.getDouble("distance_cost");
            this.optimize_by = json.getString("optimize_by");
            this.pilot = new Pilot(json.getJSONObject("pilot_detail"));
            this.slot = json.getString("slot");
            this.created_at = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault()); //2021-11-03T08:16:02.323025+00:00
            this.created_at.setTime(Objects.requireNonNull(sdf.parse(json.getString("created_at"))));
            this.current_path = null;
            this.waypoints = new ArrayList<Waypoint>();

            if (json.has("waypoints")) {
                // Create waypoints
                for (int i = 0; i < json.getJSONArray("waypoints").length(); i++) {
                    Waypoint waypoint = new Waypoint(context, json.getJSONArray("waypoints").getJSONObject(i));
                    this.waypoints.add(waypoint);

                    switch (waypoint.getLocation_type()) {
                        case "START_DEPOT": {
                            start_depot = waypoint;
                            break;
                        }
                        case "END_DEPOT": {
                            end_depot = waypoint;
                            break;
                        }
                    }
                }

                // Restore container status
                for (int i = 0; i < json.getJSONArray("containers").length(); i++) {
                    JSONObject container = json.getJSONArray("containers").getJSONObject(i);
                    Waypoint.StatusEnum new_status = Waypoint.StatusEnum.valueOf(container.getString("status"));
                    findWaypointById(container.getInt("container_id")).setStatus(new_status);
                }

            }
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

    public int getN_containers() {
        return n_containers;
    }

    public double getTime_cost() {
        return time_cost;
    }

    public double getDistance_cost() {
        return distance_cost;
    }

    public Pilot getPilot() {
        return pilot;
    }

    public String getSlot() {
        return slot;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public ArrayList<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Location getLast_known_location() {
        return last_known_location;
    }

    public DrivingPath getCurrent_path() {
        return current_path;
    }

    public Waypoint findWaypointById(int id) {
        Waypoint waypoint = null;
        for (int i = 0; i < waypoints.size(); i++) {
            waypoint= waypoints.get(i);
            if (waypoint.getId() == id) {
                break;
            }
        }
        return waypoint;
    }

    public void setLast_known_location(Location last_known_location) {
        this.last_known_location = last_known_location;
    }

    public void startRoute() {
        status = StatusEnum.RUNNING;
        toNextDestination();
        //current_path = new DrivingPath(getCurrentDestination());
    }

    public void pauseRoute() {
        status = StatusEnum.PAUSED;
        if (current_path != null) {
            current_path.reset();
            current_path.getDestination().setNext(false);
            current_path = null;
        }

    }

    public void resumeRoute() {
        status = StatusEnum.RUNNING;
        toNextDestination();
    }

    public void finishRoute() {
        status = StatusEnum.FINISHED;
    }

    public Waypoint getCurrentDestination() {
        if (current_path == null) {
            return null;
        }
        return current_path.getDestination();
    }

    public void pick_container(@NotNull Waypoint waypoint) {
        // Pick up next container
        if (waypoint.canBePicked()) {
            waypoint.pick();
        }

        toNextDestination(); // Move to next destination
    }

    public void toNextDestination() {
        Waypoint next_wp = null;
        Waypoint previous_wp = null;

        if (current_path != null) {
            previous_wp = current_path.getDestination();
        }

        // Find a pickable waypoint which has not been picked up yet
        for (int i = 0; i < waypoints.size(); i++) {
            if (( waypoints.get(i).canBePicked()) && !( waypoints.get(i).isPicked())) {
                next_wp = waypoints.get(i);
                break;
            }
        }

        if (next_wp == null) {  // If no pickable waypoints return end depot
            next_wp = end_depot;
        }

        current_path = new DrivingPath(next_wp);

        next_wp.setNext(true); // Set next property in next waypoint

        if (previous_wp != null) {
            previous_wp.setNext(false); // Set next property in previous waypoint
        }
    }


    @Override
    public String toString() {
        return "Route #" + this.id + " for " + this.slot;
    }
}
