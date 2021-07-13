/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itene.scalibur;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.itene.scalibur.custom.StretchListAdapter;
import com.itene.scalibur.custom.Utils;
import com.itene.scalibur.custom.VolleyUtils;
import com.itene.scalibur.models.Stretch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = com.itene.scalibur.MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Variables
    private int route_id;
    List<Stretch> stretchList;
    private boolean ending = false;
    private Location last_location;

    // API endpoints
    public final static String API_END_ROUTE = "http://scalibur.itene.com/api/routes/end/"; // Add <id> after
    public final static String API_GET_ROUTE_DRIVE = "http://scalibur.itene.com/api/routes/drive/"; // Add <id> after the url
    public final static String API_POST_GPS = "http://scalibur.itene.com/api/gps/";
    public final static String API_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE4ODQwNjk5MDgsImlhdCI6MTYyNDg2OTkwOCwic3ViIjoxfQ.TMo6udWhG8RvBcpMHjZZ-6z56_gK50yJvPrF2HFqkaU";

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    private Button mFinishRouteButton;
    private StretchListAdapter mAdapter;
    private RecyclerView recyclerView;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_main);

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // Load current_route
        Intent intent = getIntent();
        route_id = intent.getIntExtra("route_id",0);
        //getRouteToDrive(route_id);

        recyclerView = (RecyclerView) findViewById(R.id.routeList_view);


        mRequestLocationUpdatesButton = (Button) findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.remove_location_updates_button);
        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    if (mService != null)
                        mService.requestLocationUpdates();
                    else
                        Toast.makeText(com.itene.scalibur.MainActivity.this, "Service is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish_route(route_id);
                //mService.removeLocationUpdates();
            }
        });

        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
        if (route_id == 0)
            route_id = Utils.requestingRouteId(this);
        getRouteToDrive(route_id);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        if (!ending)
            Utils.setRequestingRouteId(this, route_id);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
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
                            ActivityCompat.requestPermissions(com.itene.scalibur.MainActivity.this,
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
            ActivityCompat.requestPermissions(com.itene.scalibur.MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                //ToDO: Change call to obtain ids from previous calls
                sendGPS(1, route_id, "GPS event", location);
                last_location = location;
                //Toast.makeText(com.itene.scalibur.MainActivity.this, Utils.getLocationText(location),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
        if (s.equals(Utils.KEY_ROUTE_ID)) {
            route_id = sharedPreferences.getInt(Utils.KEY_ROUTE_ID, 0);
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }

    private void sendGPS(int user_id, int route_id, String event, Location location) {
        try {
            JSONObject post_params = new JSONObject();
            post_params.put("user_id", user_id);
            post_params.put("route_id", route_id);
            post_params.put("event", event);
            post_params.put("lon", location.getLongitude());
            post_params.put("lat", location.getLatitude());
            post_params.put("accuracy", location.getAccuracy());
            post_params.put("speed", location.getSpeed());
            //post_params.put("api-token", API_TOKEN);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            post_params.put("time", df.format(location.getTime()));
            //post_params.put("time", df.format(new Date()));


            VolleyUtils.POST_JSON(this, API_POST_GPS, post_params, API_TOKEN, new VolleyUtils.VolleyJsonResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "GPS can not connect with the platform, error: " + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("API", "GPS uploaded to the platform: " + response.toString());
                        Toast.makeText(com.itene.scalibur.MainActivity.this, "Uploaded gps position", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRouteToDrive(int route_id) {
        try {
            VolleyUtils.GET_JSON_ARRAY(this, API_GET_ROUTE_DRIVE + route_id, API_TOKEN, new VolleyUtils.VolleyJsonArrayResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "No se puedo leer en la plataforma, error: " + message);
                    stretchList = null;
                }

                @Override
                public void onResponse(JSONArray response) {
                    try {
                        Log.d("API", "Ruta " + route_id + " leída con éxito");
                        //Toast.makeText(com.itene.scalibur.MainActivity.this, "Route " + route_id + " loaded successfully", Toast.LENGTH_SHORT).show();
                        stretchList = new ArrayList<Stretch>();
                        for (int i = 0; i < response.length(); i++)
                        {
                            JSONArray route = response.getJSONArray(i);
                            for (int j = 0; j < route.length(); j++) {
                                JSONObject stretch = route.getJSONObject(j);
                                stretchList.add(new Stretch(stretch.getInt("from"), stretch.getInt("to"),
                                        stretch.getString("from_name"), stretch.getString("to_name"),
                                        stretch.getDouble("time"), stretch.getDouble("distance" )));
                            }
                        }

                        mAdapter = new StretchListAdapter(stretchList, new StretchListAdapter.CustomItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position) {
                                //Toast.makeText(com.itene.scalibur.MainActivity.this, "Stretch clicked!", Toast.LENGTH_SHORT).show();
                                dialogRouteClicked(stretchList.get(position));
                            }
                        });
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(mAdapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dialogRouteClicked(Stretch stretch){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        // set title
        alertDialogBuilder.setTitle(getResources().getString(R.string.dialog_title_stretch));
        // set dialog message
        alertDialogBuilder
                .setMessage(getResources().getString(R.string.dialog_confirm_stretch))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        //send to platform
                        sendGPS(1, route_id, "Container recover in location #" + stretch.getTo(), last_location);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }
    private void finish_route(int route_id) {
        try {
            VolleyUtils.GET_JSON(this, API_END_ROUTE + route_id , API_TOKEN, new VolleyUtils.VolleyJsonResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "Error message, error: " + message);
                    Toast.makeText(com.itene.scalibur.MainActivity.this, "Error happened, cannot end the route rigth now", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("API", "Route ended");
                        Toast.makeText(com.itene.scalibur.MainActivity.this, "Route ended successfully", Toast.LENGTH_SHORT).show();
                        //stop sending GPS position
                        ending = true;
                        mService.removeLocationUpdates();
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
