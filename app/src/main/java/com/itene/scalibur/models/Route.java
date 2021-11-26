package com.itene.scalibur.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class Route implements Parcelable {

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
    private boolean paused;
    private boolean auto_center;

    public enum StatusEnum
    {
        READY, RUNNING, FINISHED
    }

    public Route(JSONObject json) {
        this.populate(json);
        this.paused = false;
        this.auto_center = true;
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
            this.waypoints = new ArrayList<Waypoint>();

            if (json.has("waypoints")) {
                // Create waypoints
                for (int i = 0; i < json.getJSONArray("waypoints").length(); i++) {
                    Waypoint waypoint = new Waypoint(json.getJSONArray("waypoints").getJSONObject(i));
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
            if (status.equals(StatusEnum.RUNNING) && !paused) {
                calculateNextDestination(); // sets current path
            } else {
                current_path = null;
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

    public boolean isAutoCentered() {
        return auto_center;
    }

    public void setAutoCenter(Boolean auto_center) {
        this.auto_center = auto_center;
    }

    public boolean isPaused() {
        return paused;
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
        paused = false;
        status = StatusEnum.RUNNING;
        calculateNextDestination();
        //current_path = new DrivingPath(getCurrentDestination());
    }

    public void pauseRoute() {
        paused = true;
        status = StatusEnum.RUNNING;
        if (current_path != null) {
            current_path.reset();
            waypoints.get(current_path.getDestinationIndex()).setNext(false);
            current_path = null;
        }
    }

    public void resumeRoute() {
        paused = false;
        status = StatusEnum.RUNNING;
        calculateNextDestination();
    }

    public void finishRoute() {
        status = StatusEnum.FINISHED;
    }

    public Waypoint getCurrentDestination() {
        if (current_path == null) {
            return null;
        }
        return waypoints.get(current_path.getDestinationIndex());
    }

    public void pick_container(@NotNull Waypoint waypoint) {
        // Pick up next container
        if (waypoint.couldBePicked()) {
            waypoint.pick();
        }

        calculateNextDestination(); // Move to next destination
    }

    public void skip_container(@NotNull Waypoint waypoint) {
        // Skip container if it is pickable
        if (waypoint.couldBePicked()) {
            waypoint.skip();
        }
        // If we are skipping current destination then we have to recalculate next destination
        if (getCurrentDestination() == waypoint) {
            calculateNextDestination();
        }
    }

    public void undo_container(@NotNull Waypoint waypoint) {
        // Skip container
        if (waypoint.isPicked() || waypoint.isSkipped()) {
            waypoint.undo_status();
        }

        calculateNextDestination(); // Move to next destination
    }


    // This method calculates the next waypoint based on the waypoint status
    // it should do nothing if waypoint status is not updated first
    // This should be kept private, use methods pick_container, skip_container and undo_container
    private void calculateNextDestination() {
        Waypoint next_wp = null;
        Waypoint previous_wp = null;

        // Find a pickable waypoint which has not been picked up yet
        for (int i = 0; i < waypoints.size(); i++) {
            if (waypoints.get(i).couldBePicked()) {
                next_wp = waypoints.get(i);
                break;
            }
        }

        if (next_wp == null) {  // If no pickable waypoints return end depot
            next_wp = end_depot;
        }

        if (current_path != null) {
            previous_wp = waypoints.get(current_path.getDestinationIndex());
        }

        if (next_wp != previous_wp) {
            if (current_path != null) {
                // if we are moving to next destination then drawn polyline is not longer valid
                current_path.reset();
            }
            if (next_wp != null) {
                current_path = new DrivingPath(waypoints.indexOf(next_wp));
                next_wp.setNext(true); // Set next property in next waypoint
            }

            if (previous_wp != null) {
                previous_wp.setNext(false); // Set next property in previous waypoint
            }
        }
    }

    public Integer stopsLeftToWaypoint(Waypoint waypoint) {
        Integer stops_left = 0;

        for (int i=waypoints.indexOf(getCurrentDestination()) + 1; i < waypoints.size(); i++) {
            if (waypoints.get(i).couldBePicked()) {
                stops_left +=1;
            }
            if (waypoint == waypoints.get(i)) {
                break;
            }
        }

        return stops_left;
    }


    @Override
    public String toString() {
        return "Route #" + this.id + " for " + this.slot;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeInt(this.n_containers);
        dest.writeDouble(this.time_cost);
        dest.writeDouble(this.distance_cost);
        dest.writeParcelable(this.pilot, flags);
        dest.writeString(this.optimize_by);
        dest.writeString(this.slot);
        dest.writeSerializable(this.created_at);
        dest.writeList(this.waypoints);
        dest.writeParcelable(this.last_known_location, flags);
        dest.writeParcelable(this.current_path, flags);
        dest.writeParcelable(this.start_depot, flags);
        dest.writeParcelable(this.end_depot, flags);
        dest.writeByte((byte) (paused ? 1 : 0));
        dest.writeByte((byte) (auto_center ? 1 : 0));
    }

    protected Route(Parcel in) {
        this.id = in.readInt();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : StatusEnum.values()[tmpStatus];
        this.n_containers = in.readInt();
        this.time_cost = in.readDouble();
        this.distance_cost = in.readDouble();
        this.pilot = in.readParcelable(Pilot.class.getClassLoader());
        this.optimize_by = in.readString();
        this.slot = in.readString();
        this.created_at = (Calendar) in.readSerializable();
        this.waypoints = new ArrayList<Waypoint>();
        in.readList(this.waypoints, Waypoint.class.getClassLoader());
        this.last_known_location = in.readParcelable(Location.class.getClassLoader());
        this.current_path = in.readParcelable(DrivingPath.class.getClassLoader());
        this.start_depot = in.readParcelable(Waypoint.class.getClassLoader());
        this.end_depot = in.readParcelable(Waypoint.class.getClassLoader());
        this.paused = in.readByte() != 0;
        this.auto_center = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel source) {
            return new Route(source);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };
}
