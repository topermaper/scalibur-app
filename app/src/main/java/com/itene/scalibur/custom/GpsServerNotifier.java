package com.itene.scalibur.custom;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.itene.scalibur.config.Config;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class GpsServerNotifier {
    private static final String TAG = com.itene.scalibur.custom.GpsServerNotifier.class.getSimpleName();
    private static final Long UPDATE_INTERVAL = 20L * 1000000000L; // nanoseconds
    private Context context;
    long last_update;

    public GpsServerNotifier(Context context) {
        this.last_update = System.nanoTime() - UPDATE_INTERVAL;
        this.context = context;
    }

    public void registerGpsData(Integer user_id, Integer route_id, String event, Location location) {
        long current_time = System.nanoTime();
        if (current_time - last_update > UPDATE_INTERVAL) {
            Log.d(TAG, "Sending Gps data to server" );
            sendGpsUpdate(user_id, route_id, event, location);
            last_update = current_time;
        } else {
            Log.d(TAG, "Gps not sent to server. Minimum update interval not reached yet" );
        }
    }

    private void sendGpsUpdate(Integer user_id, Integer route_id, String event, Location location) {
        try {
            JSONObject post_params = new JSONObject();
            post_params.put("user_id", user_id);
            post_params.put("route_id", route_id);
            post_params.put("event", event);
            post_params.put("lon", location.getLongitude());
            post_params.put("lat", location.getLatitude());
            post_params.put("accuracy", location.getAccuracy());
            post_params.put("speed", location.getSpeed());
            post_params.put("bearing", location.getBearing());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            post_params.put("time", df.format(location.getTime()));

            String api_token = context.getSharedPreferences("com.itene.scalibur", Context.MODE_PRIVATE).getString("API_TOKEN", null);

            Log.d("API", String.format("Sending GPS position to platform. user_id=%d route_id=%d lat=%f long=%f",
                    user_id, route_id, location.getLatitude(), location.getLongitude()));
            VolleyUtils.POST_JSON(context, Config.API_POST_GPS, post_params, api_token, new VolleyUtils.VolleyJsonResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "Can not connect with the platform, error: " + message);
                }
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("API", "GPS uploaded to the platform: " + response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }
}
