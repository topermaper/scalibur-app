package com.itene.scalibur.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;


public class Pilot implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeValue(this.zoom);
    }

    protected Pilot(Parcel in) {
        this.name = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.zoom = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<Pilot> CREATOR = new Parcelable.Creator<Pilot>() {
        @Override
        public Pilot createFromParcel(Parcel source) {
            return new Pilot(source);
        }

        @Override
        public Pilot[] newArray(int size) {
            return new Pilot[size];
        }
    };
}