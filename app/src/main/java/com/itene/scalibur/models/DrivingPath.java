package com.itene.scalibur.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Polyline;

public class DrivingPath implements Parcelable {

    Polyline path_polyline;
    Integer destination_index;
    Double distance;
    Double duration;

    DrivingPath(Integer index) {
        this.destination_index = index;
    }

    public Polyline get_polyline() {
        return path_polyline;
    }

    public void setPolyline(Polyline path_polyline) {
        this.path_polyline = path_polyline;
    }

    public Integer getDestinationIndex() {
        return destination_index;
    }

    public void setDestinationIndex(Integer index) {
        this.destination_index = index;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeParcelable(this.path_polyline, flags);
        dest.writeValue(this.destination_index);
        dest.writeValue(this.distance);
        dest.writeValue(this.duration);
    }

    protected DrivingPath(Parcel in) {
        //this.path_polyline = in.readParcelable(Polyline.class.getClassLoader());
        this.destination_index = (Integer)in.readValue(Integer.class.getClassLoader());
        this.distance = (Double) in.readValue(Double.class.getClassLoader());
        this.duration = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Parcelable.Creator<DrivingPath> CREATOR = new Parcelable.Creator<DrivingPath>() {
        @Override
        public DrivingPath createFromParcel(Parcel source) {
            return new DrivingPath(source);
        }

        @Override
        public DrivingPath[] newArray(int size) {
            return new DrivingPath[size];
        }
    };
}
