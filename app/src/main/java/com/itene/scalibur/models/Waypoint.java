package com.itene.scalibur.models;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.itene.scalibur.R;
import org.json.JSONException;
import org.json.JSONObject;

import static com.itene.scalibur.custom.Utils.BitmapFromVector;


public class Waypoint extends Location {
    private static final String TAG = "Waypoint";
    protected Integer id;
    private String location_type;
    private StatusEnum status;
    private Boolean next;
    Context context;

    public enum StatusEnum
    {
        PICKED, SKIPPED, WAITING
    }


    public Waypoint(Context context, JSONObject json) {
        super(json);
        try {
            this.context = context;
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

    public void pick() {
        status = StatusEnum.PICKED;
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

    public Boolean canBePicked() {
        return location_type.equals("CONTAINER_SPOT");
    }

    public void updateMarker() {
        if (marker == null) {
            Log.w(TAG, "Null marker can not be updated");
            return;
        }

        switch (location_type) {
            case "START_DEPOT":
            case "END_DEPOT": {
                marker.setIcon(BitmapFromVector(context, R.drawable.depot64));
                break;
            }
            case "CONTAINER_SPOT": {
                if (status == StatusEnum.PICKED) {
                    marker.setIcon(BitmapFromVector(context, R.drawable.container64_blue));
                } else if (next) {
                    marker.setIcon(BitmapFromVector(context, R.drawable.container64_red));
                } else {
                    marker.setIcon(BitmapFromVector(context, R.drawable.container64));
                }
            }
        }
    }
}
