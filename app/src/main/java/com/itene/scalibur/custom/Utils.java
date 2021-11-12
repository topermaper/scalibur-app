

package com.itene.scalibur.custom;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.itene.scalibur.R;
import com.itene.scalibur.config.Config;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    public static final String KEY_ROUTE_ID = "request_route_id";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static int requestingRouteId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_ROUTE_ID, 0);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingRouteId The location updates state.
     */
    public static void setRequestingRouteId(Context context, int requestingRouteId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(KEY_ROUTE_ID, requestingRouteId)
                .apply();
    }

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ") \n Accuracy: " + location.getAccuracy() + "m \n Speed: " + location.getSpeed() + " m/s \n Time: " + location.getTime();
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    public static void sendGPS(Context context, int user_id, int route_id, String event, android.location.Location location) {
        try {
            JSONObject post_params = new JSONObject();
            post_params.put("user_id", user_id);
            post_params.put("route_id", route_id);
            post_params.put("event", event);
            post_params.put("lon", location.getLongitude());
            post_params.put("lat", location.getLatitude());
            post_params.put("accuracy", location.getAccuracy());
            post_params.put("speed", location.getSpeed());
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);
        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);
        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
