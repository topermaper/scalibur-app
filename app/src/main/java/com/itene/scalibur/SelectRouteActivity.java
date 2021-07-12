package com.itene.scalibur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itene.scalibur.custom.VolleyUtils;
import com.itene.scalibur.models.Route;
import com.itene.scalibur.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectRouteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public final static String API_GET_CONFIRMED_ROUTES = "http://scalibur.itene.com/api/routes/Kozani/confirmed/list";
    public final static String API_START_ROUTE = "http://scalibur.itene.com/api/routes/start/"; // + route_id
    public final static String API_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE4ODQwNjk5MDgsImlhdCI6MTYyNDg2OTkwOCwic3ViIjoxfQ.TMo6udWhG8RvBcpMHjZZ-6z56_gK50yJvPrF2HFqkaU";

    private Spinner sp_choose_route;
    private CardView route_card;
    private TextView tv_title, tv_time, tv_containers, tv_distance, tv_pilot, tv_slot;
    private Button mStartRouteButton;
    private Route SelectedRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_route);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Configure spinner
        sp_choose_route = (Spinner) findViewById(R.id.sp_choose_route);
        sp_choose_route.setOnItemSelectedListener(this);
        // Load possible routes
        getRoutesConfirmed();
        // Route card elements
        route_card = (CardView) findViewById(R.id.route_card);
        tv_title = (TextView) findViewById(R.id.route_title);
        tv_time = (TextView) findViewById(R.id.route_time_result);
        tv_distance = (TextView) findViewById(R.id.route_distance_result);
        tv_pilot = (TextView) findViewById(R.id.route_pilot_result);
        tv_slot = (TextView) findViewById(R.id.route_slot_result);
        tv_containers = (TextView) findViewById(R.id.route_containers_result);

        mStartRouteButton = (Button) findViewById(R.id.start_route);
        mStartRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SelectedRoute != null) {
                    startRoute(SelectedRoute.getId());
                } else {
                    //TODO: Remove this else, only debug purpose
                    startRoute(6);
                }

            }
        });
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        SelectedRoute = (Route) adapterView.getSelectedItem();
        if(SelectedRoute != null) {
            tv_slot.setText(SelectedRoute.getSlot());
            tv_title.setText(String.format("Route #%d", SelectedRoute.getId()));
            tv_time.setText(String.format("%.0f s", SelectedRoute.getTime_cost()));
            tv_distance.setText(String.format("%.0f m", SelectedRoute.getDistance_cost()));
            tv_containers.setText(String.format("%d", SelectedRoute.getN_containers()));
            tv_pilot.setText(SelectedRoute.getPilot());
            route_card.setVisibility(View.VISIBLE);
        }
        else
            route_card.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void getRoutesConfirmed() {
        try {
            VolleyUtils.GET_JSON_ARRAY(this, API_GET_CONFIRMED_ROUTES, API_TOKEN, new VolleyUtils.VolleyJsonArrayResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "No se puede leer en la plataforma, error: " + message);
                }

                @Override
                public void onResponse(JSONArray response) {
                    try {
                        Log.d("API", "Rutas confirmadas de Kozani");
                        Toast.makeText(com.itene.scalibur.SelectRouteActivity.this, "Rutas confirmadas de Kozani", Toast.LENGTH_SHORT).show();
                        List <Route> mRouteList = new ArrayList<Route>();
                        for (int i = 0; i < response.length(); i++)
                        {
                            mRouteList.add(new Route(response.getJSONObject(i)));
                        }
                        ArrayAdapter<Route> adapter_choose_route = new ArrayAdapter<Route>(getApplicationContext(), android.R.layout.simple_spinner_item, mRouteList);
                        //ArrayAdapter<CharSequence> adapter_choose_route = ArrayAdapter.createFromResource(this, R.array.route_options, android.R.layout.simple_spinner_item);
                        adapter_choose_route.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_choose_route.setAdapter(adapter_choose_route);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRoute(int route_id) {
        try {
            VolleyUtils.GET_JSON(this, API_START_ROUTE + route_id , API_TOKEN, new VolleyUtils.VolleyJsonResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "No se puede leer en la plataforma, error: " + message);
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("API", "Ruta iniciada");
                        Toast.makeText(com.itene.scalibur.SelectRouteActivity.this, "Route started successfully", Toast.LENGTH_SHORT).show();
                        Intent myIntent = new Intent(SelectRouteActivity.this, MainActivity.class);
                        myIntent.putExtra("route_id", route_id); //Optional parameters
                        startActivity(myIntent);
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