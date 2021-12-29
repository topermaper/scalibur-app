package com.itene.scalibur.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Marker;
import com.itene.scalibur.R;

import org.json.JSONException;
import org.json.JSONObject;


public class Location implements Parcelable {

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

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        //dest.writeParcelable(this.marker, flags);
    }

    protected Location(Parcel in) {
        this.name = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        //this.marker = in.readParcelable(Marker.class.getClassLoader());
    }

}
