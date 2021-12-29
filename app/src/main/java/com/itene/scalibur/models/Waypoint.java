package com.itene.scalibur.models;

import android.os.Parcel;
import android.util.Log;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.itene.scalibur.R;
import org.json.JSONException;
import org.json.JSONObject;


public class Waypoint extends Location {
    private static final String TAG = "Waypoint";
    protected Integer id;
    private String location_type;
    private StatusEnum status;
    private Boolean next;

    public enum StatusEnum
    {
        PICKED, SKIPPED, WAITING
    }


    public Waypoint(JSONObject json) {
        super(json);
        try {
            this.id = json.getInt("id");
            this.status = StatusEnum.WAITING;
            this.next = false;
            this.location_type = json.getString("locat_type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Integer getId() {
        return id;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocation_type() {
        return location_type;
    }

    public String getPrettyName() {
        return String.format("#%d %s", id, name);
    }

    public Boolean isPicked() {
        if (status == StatusEnum.PICKED){
            return true;
        } else {
            return false;
        }
    }

    public Boolean isSkipped() {
        if (status == StatusEnum.SKIPPED){
            return true;
        } else {
            return false;
        }
    }

    public boolean isPickable() {
        return location_type.equals("CONTAINER_SPOT");
    }

    public void pick() {
        status = StatusEnum.PICKED;
        updateMarker();
    }

    public void skip() {
        next=false;
        status = StatusEnum.SKIPPED;
        updateMarker();
    }

    public void undo_status() {
        status = StatusEnum.WAITING;
        updateMarker();
    }

    public void setNext(Boolean b) {
        next = b;
        updateMarker();
    }

    @Override
    public void setMarker(Marker marker) {
        this.marker = marker;
        updateMarker();
    }


    public Boolean couldBePicked() {
        return (location_type.equals("CONTAINER_SPOT") && status.equals(StatusEnum.WAITING));
    }

    public void updateMarker() {
        if (marker == null) {
            Log.w(TAG, "Null marker can not be updated");
            return;
        }

        switch (location_type) {
            case "START_DEPOT":
            case "END_DEPOT": {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.depot64));
                break;
            }
            case "CONTAINER_SPOT": {
                if (status == StatusEnum.PICKED) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.container64_blue));
                } else if (status == StatusEnum.SKIPPED) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.container64_yellow));
                } else if (next) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.container64_red));
                } else {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.container64));
                }
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(this.id);
        dest.writeString(this.location_type);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeValue(this.next);
    }

    protected Waypoint(Parcel in) {
        super(in);
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.location_type = in.readString();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : StatusEnum.values()[tmpStatus];
        this.next = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<Waypoint> CREATOR = new Creator<Waypoint>() {
        @Override
        public Waypoint createFromParcel(Parcel source) {
            return new Waypoint(source);
        }

        @Override
        public Waypoint[] newArray(int size) {
            return new Waypoint[size];
        }
    };
}
