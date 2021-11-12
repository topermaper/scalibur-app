package com.itene.scalibur.models;

import com.google.android.gms.maps.model.Polyline;

public class DrivingPath {

    Polyline path_polyline;
    Waypoint destination;
    Double distance;
    Double duration;

    DrivingPath(Waypoint destination) {
        this.destination = destination;
    }

    public Polyline get_polyline() {
        return path_polyline;
    }

    public void setPolyline(Polyline path_polyline) {
        this.path_polyline = path_polyline;
    }

    public Waypoint getDestination() {
        return destination;
    }

    public void setDestination(Waypoint destination) {
        this.destination = destination;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public void reset() {
        if (path_polyline != null) {
            path_polyline.remove();
        }
        duration = null;
        distance = null;
    }

    public String getPrettyDistance() {
        String pretty_distance;

        if (distance == null) {
            return "Unknown";
        }
        if (distance < 1000) {
            pretty_distance = String.format("%.0fm", distance);
        } else {
            pretty_distance = String.format("%.2fKm", distance/1000);
        }

        return pretty_distance;
    }

    public String getPrettyDuration() {
        String pretty_duration;

        if (duration == null) {
            return "Unknown";
        }
        if (duration < 60) {
            pretty_duration = String.format("%.0fs", duration);
        } else if(duration < 3600) {
            pretty_duration = String.format("%.0fm%.0fs", duration/60, duration%60 );
        } else {
            pretty_duration = String.format("%.0fh%.0fm%.0fs", duration/3600,  (duration % 3600)/60, duration%60);
        }

        return pretty_duration;
    }
}
