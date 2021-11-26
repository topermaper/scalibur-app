package com.itene.scalibur;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.PolyUtil;
import com.itene.scalibur.config.RoutingMachine;
import com.itene.scalibur.custom.Utils;
import com.itene.scalibur.custom.VolleyUtils;
import com.itene.scalibur.config.Config;
import com.itene.scalibur.custom.WaypointListAdapter;
import com.itene.scalibur.data.model.LoggedInUser;
import com.itene.scalibur.models.DrivingPath;
import com.itene.scalibur.models.Route;
import com.itene.scalibur.models.Location;
import com.itene.scalibur.models.Waypoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;
import android.content.SharedPreferences;

//import static com.itene.scalibur.custom.Utils.BitmapFromVector;

public class RouteMapActivity extends AppCompatActivity implements GoogleMap.OnCameraMoveStartedListener, OnMapReadyCallback {
    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private static final String TAG = com.itene.scalibur.RouteMapActivity.class.getSimpleName();

    private GoogleMap mMap;
    private ImageView recording_iv;
    private ImageButton get_directions_iv;
    private ImageButton autocenter_ib;
    private Route route;
    private Integer route_id;
    private LoggedInUser user;
    private Animation recording_animation;
    private LinearLayoutManager lManager;
    private RecyclerView waypoint_rv;
    //private Boolean auto_center_cam;
    private RouteMapActivity.MyReceiver myReceiver; // The BroadcastReceiver used to listen from broadcasts from the service.
    private LocationUpdatesService mService = null; // A reference to the service used to get location updates.
    private boolean mBound = false;    // Tracks the bound state of the service.
    private SharedPreferences sharedPreferences;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            // This starts location updates inmediately for running routes
            if (route.getStatus().equals(Route.StatusEnum.RUNNING) && !route.isPaused()) {
                Log.d(TAG, "onServiceConnected - requestLocationUpdates");
                mService.requestLocationUpdates();
            } else {
                Log.d(TAG, "onServiceConnected - removeLocationUpdates");
                mService.removeLocationUpdates();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mService = null;
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("API", "onCreate");
        super.onCreate(savedInstanceState);

        route = getIntent().getParcelableExtra("route");
        user = getIntent().getParcelableExtra("user");

        sharedPreferences = this.getSharedPreferences(
                "com.itene.scalibur", Context.MODE_PRIVATE);

        // Set US locale, converts doubles to string using dot
        Locale.setDefault(Locale.US);

        //auto_center_cam=true;

        setContentView(R.layout.route_map);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        waypoint_rv = (RecyclerView) findViewById(R.id.waypoint_rv);
        recording_iv = (ImageView) findViewById(R.id.recording);
        get_directions_iv = (ImageButton) findViewById(R.id.get_directions_ib);
        autocenter_ib = (ImageButton) findViewById(R.id.autocenter_ib);

        myReceiver = new RouteMapActivity.MyReceiver();

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        waypoint_rv.setHasFixedSize(true);
        lManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        waypoint_rv.setLayoutManager(lManager);

        // Use PagerSnapHelper to display a single item in the recycler view
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(waypoint_rv);

        // Set waypoint recycler view adapter
        WaypointListAdapter waypoint_adapter = new WaypointListAdapter(route, RouteMapActivity.this);
        waypoint_rv.setAdapter(waypoint_adapter);

        recording_animation = new AlphaAnimation(0, 1); //to change visibility from invisible to visible

        // addOnScrollListener is called when recycler view is scrolled manually by the user
        // not called when calling setCurrentItem
        waypoint_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, String.format("addOnScrollListener newState=%d", newState));
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    int position = getCurrentItem();
                    Marker marker = route.getWaypoints().get(position).getMarker();
                    marker.showInfoWindow();
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 250, null);
                    route.setAutoCenter(false);
                    autocenter_ib.setVisibility(View.VISIBLE);
                    waypoint_rv.getAdapter().notifyDataSetChanged();
                }
            }
        });

        get_directions_iv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Waypoint waypoint = route.getWaypoints().get(getCurrentItem());
                String string = String.format("google.navigation:q=%f,%f", waypoint.getLatitude(), waypoint.getLongitude());
                Uri uri = Uri.parse(string);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Service not available. Can not get directions.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        autocenter_ib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Location location = route.getLast_known_location();

                if (location == null) { // Location could be null if GPS is disabled and the app would crash
                    Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Unable to retrieve GPS location", Toast.LENGTH_SHORT).show();
                    return;
                }

                route.setAutoCenter(true);
                autocenter_ib.setVisibility(View.GONE);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                setCurrentItem(route.getWaypoints().indexOf(route.getCurrentDestination()), false);
                // Notify adapter data has change so it redraws
                waypoint_rv.getAdapter().notifyDataSetChanged();
            }
        });

        updateRoute(null);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

        //PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // Pass user reference via intent extras to the location updates service
        Intent intent = new Intent(this, LocationUpdatesService.class);
        intent.putExtra("user", user);
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        /*
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
         */
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        // Stops updates when the activity is destroyed
        // Prevents service to become foreground service and send API updates
        if (mService != null) {
            mService.removeLocationUpdates();
        }

        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");

        // Save route
        outState.putParcelable("route",  route);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");
        route = savedInstanceState.getParcelable("route");
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        Utils.setRequestingRouteId(this, route.getId());
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.route_map_menu, menu);
        return true;
    }

    public void onUserMapCenterChange() {
        // User touched the map, don´t center the camera in the current position
        // Only during running status
        if (route.getStatus() == Route.StatusEnum.RUNNING && !route.isPaused()) {
            route.setAutoCenter(false);
            autocenter_ib.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        String reasonText = "UNKNOWN_REASON";

        switch (reason) {
            case GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE:
                reasonText = "GESTURE";
                onUserMapCenterChange();
                break;
            case GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION:
                reasonText = "API_ANIMATION";
                break;
            case GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION:
                reasonText = "DEVELOPER_ANIMATION";
                break;
        }
        Log.d(TAG, "onCameraMoveStarted(" + reasonText + ")");
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        MenuItem start_route = menu.findItem(R.id.start_route);
        MenuItem pause_route = menu.findItem(R.id.pause_route);
        MenuItem resume_route = menu.findItem(R.id.resume_route);
        MenuItem finish_route = menu.findItem(R.id.finish_route);

        start_route.setVisible(false);
        pause_route.setVisible(false);
        resume_route.setVisible(false);
        finish_route.setVisible(false);

        Route.StatusEnum route_status = route.getStatus();
        if (route_status == null) {
            return true;
        }

        switch (route_status) {
            case READY: {
                start_route.setVisible(true);
                break;
            }
            case RUNNING: {
                if (route.isPaused()) {
                    resume_route.setVisible(true);
                } else {
                    pause_route.setVisible(true);
                }
                finish_route.setVisible(true);
                break;
            }
            case FINISHED: {
                break;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.start_route:
                startRoute();
                break;
            case R.id.resume_route:
                resumeRoute();
                break;
            case R.id.pause_route:
                pauseRoute();
                break;
            case R.id.finish_route:
                finishRoute();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void pauseRoute() {
        Log.i(TAG, "Pause route");

        if (mService != null) {
            mService.removeLocationUpdates();
            route.pauseRoute();
            updateRoute(null);
        } else {
            Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Service is not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRoute() {
        Log.d(TAG, "Start route");
        if (!checkPermissions()) {
            requestPermissions();
        }

        if (mService != null) {
            try {
                String api_url = String.format(Config.API_START_ROUTE, route.getId());
                Log.d("API", String.format("Sending request to %s", api_url));

                String api_token = sharedPreferences.getString("API_TOKEN", null);
                VolleyUtils.GET_JSON(this, api_url, api_token, new VolleyUtils.VolleyJsonResponseListener() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Could not start route. Request error.", Toast.LENGTH_SHORT).show();
                        Log.e("API", "No se puede leer en la plataforma, error: " + message);
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API:", String.format("Response: %s", response.toString()));
                        route.startRoute();
                        mService.requestLocationUpdates();
                        updateRoute(null);
                    }
                });
            } catch (Exception e) {
                Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Could not start route. Can not connect to server.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Could not start route. Service is not available.", Toast.LENGTH_SHORT).show();
        }
    }


    private void finishRoute() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Do you want to finish this route?");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (mService != null) {
                    mService.removeLocationUpdates();
                } else {
                    Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Service is not available", Toast.LENGTH_SHORT).show();
                }

                try {
                    String api_url = String.format(Config.API_END_ROUTE, route.getId());
                    Log.d("API", String.format("Sending request to %s", api_url));

                    String api_token = sharedPreferences.getString("API_TOKEN", null);
                    VolleyUtils.GET_JSON(getApplicationContext(), api_url, api_token, new VolleyUtils.VolleyJsonResponseListener() {
                        @Override
                        public void onError(String message) {
                            Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Could not finish route. Request error.", Toast.LENGTH_SHORT).show();
                            Log.e("API", "No se puede leer en la plataforma, error: " + message);
                        }

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("API:", String.format("Response: %s", response.toString()));
                            route.finishRoute();
                            Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Route finished", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Could not start route. Can not connect to server.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        return;
                    }
                });

        alert.show();
    }

    private void resumeRoute() {
        Log.d(TAG, "Resume route");
        if (!checkPermissions()) {
            requestPermissions();
        }

        if (mService != null) {
            route.resumeRoute();
            mService.requestLocationUpdates();
            updateRoute(null);
        } else {
            Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Service is not available", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(TAG, "onSharedPreferenceChanged");
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            //setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES, false));
        }
        if (s.equals(Utils.KEY_ROUTE_ID)) {
            route.setId(sharedPreferences.getInt(Utils.KEY_ROUTE_ID, 0));
        }
    }
    */

    public boolean hasPrevious() {
        return getCurrentItem() > 0;
    }

    public boolean hasNext() {
        return waypoint_rv.getAdapter() != null &&
                getCurrentItem() < (waypoint_rv.getAdapter().getItemCount()- 1);
    }

    public void get_previous() {
        int position = getCurrentItem();
        if (position > 0)
            setCurrentItem(position -1, true);
    }

    public void get_next() {
        RecyclerView.Adapter adapter = waypoint_rv.getAdapter();
        if (adapter == null)
            return;

        int position = getCurrentItem();
        int count = adapter.getItemCount();
        if (position < (count -1))
            setCurrentItem(position + 1, true);
    }

    private int getCurrentItem(){
        return ((LinearLayoutManager)waypoint_rv.getLayoutManager())
                .findFirstVisibleItemPosition();
    }

    public void setCurrentItem(int position, boolean smooth) {
        try {
            route.getWaypoints().get(position).getMarker().showInfoWindow();
        } catch(Exception e){
            Log.w(TAG, "Null marker. Can not show info window");
        }
        if (smooth)
            waypoint_rv.smoothScrollToPosition(position);
        else
            waypoint_rv.scrollToPosition(position);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnCameraMoveStartedListener(this);

        // adding on click listener to marker of google maps.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, "onMarkerClick");
                // on marker click we are getting the title of our marker
                // which is clicked and displaying it in a toast message.
                Waypoint waypoint = (Waypoint)marker.getTag();
                setCurrentItem(route.getWaypoints().indexOf(waypoint), false);
                marker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 250, null);
                onUserMapCenterChange();
                // Return false to continue with the default behaviour
                return true;
            }
        });

        addMarkers(mMap, route);

        // Move camera to the pilot center
        LatLng pilot_latlng = new LatLng(route.getPilot().getLatitude(), route.getPilot().getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pilot_latlng, route.getPilot().getZoom()));
    }

    /**
     * Given a map and a route, adds all the route waypoints as markers in the map
     */
    private void addMarkers(GoogleMap mMap, Route route) {
        Waypoint waypoint;
        LatLng coordinates;
        ArrayList<Waypoint> waypoints = route.getWaypoints();

        for (int i = 0; i < waypoints.size(); i++ ) {
            waypoint =  waypoints.get(i);
            coordinates = new LatLng(waypoint.getLatitude(), waypoint.getLongitude());

            Marker marker = mMap.addMarker(new MarkerOptions().position(coordinates).anchor(0.5f,0.5f).title(waypoint.getPrettyName()));
            marker.setTag(waypoint);

            // Add marker to waypoint
            waypoint.setMarker(marker);
        }
    }

    /*
    private void loadRouteFromServer() {
        try {
            String api_url = String.format("%s%d", Config.API_ROUTES, route.getId());
            String api_token = sharedPreferences.getString("API_TOKEN", null);
            VolleyUtils.GET_JSON(this, api_url, api_token, new VolleyUtils.VolleyJsonResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "No se puede leer en la plataforma, error: " + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("API", "Rutas confirmadas de Kozani");
                        Log.d("API response:", response.toString());

                        route.populate(response);
                        // Pause status doesn´t exist for the API so when route data is returned we have to
                        // change the status to pause in order to get the app menu to display the correct options
                        if (route.getStatus() == Route.StatusEnum.RUNNING) {
                            route.pauseRoute();
                        }
                        invalidateOptionsMenu(); // route has now the correct status so menu has to be redrawn
                        addMarkers(mMap, route);

                        // Move camera to the pilot center
                        LatLng pilot_latlng = new LatLng(route.getPilot().getLatitude(), route.getPilot().getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pilot_latlng, route.getPilot().getZoom()));

                        WaypointListAdapter waypoint_adapter = new WaypointListAdapter(route, RouteMapActivity.this);
                        waypoint_rv.setAdapter(waypoint_adapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


    private void blinkGpsLocationReceivedIcon() {
        recording_animation.setDuration(1000); //1 second duration for each animation cycle
        recording_animation.setInterpolator(new LinearInterpolator());
        recording_animation.setRepeatCount(1); //repeating indefinitely
        recording_animation.setRepeatMode(Animation.REVERSE);
        recording_iv.startAnimation(recording_animation); //to start animation
    }


    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            android.location.Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Log.d("API", "Receiving GPS location ...");
                blinkGpsLocationReceivedIcon();
                Utils.sendGPS(getApplicationContext(), user.getId(), route.getId(), "GPS event", location);
                updateRoute(location);
            }
        }
    }

    private void updateRoute(android.location.Location location) {
        if (location != null) {
            LatLng current_position = new LatLng(location.getLatitude(), location.getLongitude());

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.garbage_truck);
            bitmap = Utils.rotateAndFlipTruckBitmap(bitmap, location.getBearing());

            try { // Remove last known location marker if exists
                route.getLast_known_location().getMarker().remove();
            } catch (Exception e) {
                Log.w(TAG, "Can not remove last location marker. Null location or marker");
            }

            // Add current position truck marker
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(current_position)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

            route.setLast_known_location(new Location(location));
            route.getLast_known_location().setMarker(marker);

            if (route.isAutoCentered()) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(current_position));
            }
        }
        // ************************************************************

        // Remove truck icon when not running
        if (!route.getStatus().equals(Route.StatusEnum.RUNNING) || route.isPaused()) {
            try { // Remove last known location marker if exists
                route.getLast_known_location().getMarker().remove();
            } catch (Exception e) {
                Log.w(TAG, "Can not remove last location marker. Null location or marker");
            }
        }

        if (route.isAutoCentered()) {
            autocenter_ib.setVisibility(View.GONE);
            Integer next_wp_index = route.getWaypoints().indexOf(route.getCurrentDestination());
            if (getCurrentItem() != next_wp_index) {
                // Move recycler view to the new waypoint
                setCurrentItem(next_wp_index, false);
            }
        } else {
            autocenter_ib.setVisibility(View.VISIBLE);
        }

        // We are asking the route machine for a solution so we need location and next waypoint to be not null
        // and the route to be in "Running" status and not paused
        Location next_waypoint = route.getCurrentDestination();
        if (location != null && next_waypoint != null &&  !route.isPaused() && route.getStatus().equals(Route.StatusEnum.RUNNING)) {
            String current_waypoint_str = String.format("%s,%s", location.getLongitude(), location.getLatitude());
            String next_waypoint_str = String.format("%s,%s", next_waypoint.getLongitude(), next_waypoint.getLatitude());
            String url = String.format(Config.API_RM_DRIVING, String.format("%s;%s",current_waypoint_str, next_waypoint_str));

            VolleyUtils.GET_JSON(this, url, null, new VolleyUtils.VolleyJsonResponseListener() {
                @Override
                public void onError(String message) {
                    Toast.makeText(com.itene.scalibur.RouteMapActivity.this, "Can not connect with routing machine", Toast.LENGTH_SHORT).show();
                    Log.e("RoutingMachine", "Can not connect with routing machine: " + message);
                }

                @Override
                public void onResponse(JSONObject response) {

                    try {
                        Log.d("RoutingMachine", "Received solution from routing platform: " + response.toString());
                        JSONArray routes = response.getJSONArray("routes");
                        String geometry = routes.getJSONObject(0).getString("geometry");

                        // Remove current polyline so we can draw a new one
                        DrivingPath current_path = route.getCurrent_path();
                        current_path.reset();

                        //Draw new polyline
                        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                                .clickable(true).addAll(PolyUtil.decode(geometry)));

                        current_path.setPolyline(polyline);
                        current_path.setDistance(routes.getJSONObject(0).getDouble("distance"));
                        current_path.setDuration(routes.getJSONObject(0).getDouble("duration"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Notify adapter data has change so it redraws
                    waypoint_rv.getAdapter().notifyDataSetChanged();
                }
            });
        }
        // Notify adapter data has change so it redraws
        waypoint_rv.getAdapter().notifyDataSetChanged();
    }
    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(com.itene.scalibur.RouteMapActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(com.itene.scalibur.RouteMapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}
