

package com.itene.scalibur.custom;

import com.itene.scalibur.R;
import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

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
}
