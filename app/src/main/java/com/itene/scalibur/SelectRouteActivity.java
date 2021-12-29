package com.itene.scalibur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itene.scalibur.custom.RouteListAdapter;
import com.itene.scalibur.custom.VolleyUtils;
import com.itene.scalibur.data.model.LoggedInUser;
import com.itene.scalibur.models.Route;
import com.itene.scalibur.ui.login.LoginActivity;
import com.itene.scalibur.config.Config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectRouteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = com.itene.scalibur.SelectRouteActivity.class.getSimpleName();
    private LinearLayoutManager lManager;
    private RecyclerView rv_routeList;
    private Spinner sp_choose_route;
    private CardView route_card;
    private TextView tv_title, tv_time, tv_containers, tv_distance, tv_pilot, tv_slot;
    private Button mStartRouteButton;
    private Route SelectedRoute;
    private LoggedInUser user;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = getIntent().getParcelableExtra("user");
        sharedPreferences = this.getSharedPreferences(
                "com.itene.scalibur", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_select_route);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Configure spinner
        /*
        sp_choose_route = (Spinner) findViewById(R.id.sp_choose_route);
        sp_choose_route.setOnItemSelectedListener(this);
        */

        // Route card
        //route_card = (CardView) findViewById(R.id.route_card);
        // Obtener el Recycler

        rv_routeList = (RecyclerView) findViewById(R.id.rv_routeList);
        rv_routeList.setHasFixedSize(true);
        // Add click listener

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        rv_routeList.setLayoutManager(lManager);

        // Load possible routes
        getRoutesConfirmed();
        // Route card elements
        //route_card = (CardView) findViewById(R.id.route_card);
        tv_title = (TextView) findViewById(R.id.route_title);
        tv_time = (TextView) findViewById(R.id.route_time_result);
        tv_distance = (TextView) findViewById(R.id.route_distance_result);

        Toast.makeText(SelectRouteActivity.this, String.format("Hi %s, please select a route", user.getName()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        SelectedRoute = (Route) adapterView.getSelectedItem();
        if(SelectedRoute != null) {
            tv_slot.setText(SelectedRoute.getSlot());
            tv_title.setText(String.format("Route #%d", SelectedRoute.getId()));
            tv_time.setText(String.format("%.0f s", SelectedRoute.getTime_cost()));
            tv_distance.setText(String.format("%.0f m", SelectedRoute.getDistance_cost()));
            tv_containers.setText(String.format("%d", SelectedRoute.getN_containers()));
            tv_pilot.setText(SelectedRoute.getPilot().getName());
            route_card.setVisibility(View.VISIBLE);
        }
        else
            route_card.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_route_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void logout() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                "com.itene.scalibur", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("API_TOKEN").apply();

        Intent myIntent = new Intent(this, LoginActivity.class);
        startActivity(myIntent);
        setResult(Activity.RESULT_OK);
        //Complete and destroy login activity once successful
        finish();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    private void getRoutesConfirmed() {
        try {
            String api_token = sharedPreferences.getString("API_TOKEN", null);
            VolleyUtils.GET_JSON_ARRAY(this, Config.API_GET_ROUTES, api_token, new VolleyUtils.VolleyJsonArrayResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "No se puede leer en la plataforma, error: " + message);
                }

                @Override
                public void onResponse(JSONArray response) {
                    try {
                        Log.d("API", "Rutas confirmadas de Kozani");
                        //Toast.makeText(com.itene.scalibur.SelectRouteActivity.this, "Rutas confirmadas de Kozani", Toast.LENGTH_SHORT).show();
                        List <Route> mRouteList = new ArrayList<Route>();
                        for (int i = 0; i < response.length(); i++)
                        {
                            mRouteList.add(new Route(response.getJSONObject(i)));
                        }

                        RouteListAdapter adapter_route_card = new RouteListAdapter(mRouteList, SelectRouteActivity.this);
                        rv_routeList.setAdapter(adapter_route_card);

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