package com.itene.scalibur.custom;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.itene.scalibur.R;
import com.itene.scalibur.RouteMapActivity;
import com.itene.scalibur.SelectRouteActivity;
import com.itene.scalibur.config.Config;
import com.itene.scalibur.models.Route;


import org.json.JSONObject;

import java.util.List;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.RouteListHolder> {

    private static final String TAG = com.itene.scalibur.custom.RouteListAdapter.class.getSimpleName();
    List<Route> routes;
    SelectRouteActivity context;

    public RouteListAdapter(List<Route> routes, SelectRouteActivity activity){
        this.routes = routes;
        this.context = activity;
    }

    @Override
    public RouteListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_card, parent, false);
        RouteListHolder pvh = new RouteListHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(RouteListHolder holder, int position) {
        Route route = this.routes.get(position);
        holder.routeTitle.setText(String.format("Route %d - %s", route.getId(), route.getStatus().toString()));
        holder.routeTime.setText(String.format("%.0f s", route.getTime_cost()));
        holder.routeDistanceResult.setText(String.format("%.0f m", route.getDistance_cost()));
        holder.routeSlotResult.setText(route.getSlot());
        holder.routeContainerResult.setText(String.format("%d", route.getN_containers()));
        holder.routePilotResult.setText(route.getPilot().getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, String.format("Clicked on route #%d", route.getId()));

                try {
                    String api_url = String.format("%s%d", Config.API_ROUTES, route.getId());
                    String api_token =  context.getSharedPreferences("com.itene.scalibur", Context.MODE_PRIVATE).getString("API_TOKEN", null);
                    VolleyUtils.GET_JSON(context, api_url, api_token, new VolleyUtils.VolleyJsonResponseListener() {
                        @Override
                        public void onError(String message) {
                            Log.e("API", "No se puede leer en la plataforma, error: " + message);
                        }

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, response.toString());

                                route.populate(response);

                                Intent intent = new Intent(context, RouteMapActivity.class);
                                // Pass populated route to the new activity
                                intent.putExtra("route", route);

                                // Pass logged user
                                intent.putExtra("user", (Parcelable)context.getIntent().getParcelableExtra("user"));
                                context.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class RouteListHolder extends RecyclerView.ViewHolder {
        TextView routeTitle;
        TextView routeTime;
        TextView routeDistanceResult;
        TextView routeSlotResult;
        TextView routeContainerResult;
        TextView routePilotResult;

        RouteListHolder(View itemView) {
            super(itemView);

            routeTitle = (TextView)itemView.findViewById(R.id.route_title);
            routeTime = (TextView)itemView.findViewById(R.id.route_time_result);
            routeDistanceResult = (TextView)itemView.findViewById(R.id.route_distance_result);
            routeContainerResult = (TextView)itemView.findViewById(R.id.route_containers_result);
            routeSlotResult = (TextView)itemView.findViewById(R.id.route_slot_result);
            routePilotResult = (TextView)itemView.findViewById(R.id.route_pilot_result);
        }
    }

}