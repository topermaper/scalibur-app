package com.itene.scalibur.custom;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.itene.scalibur.R;
import com.itene.scalibur.RouteMapActivity;
import com.itene.scalibur.config.Config;
import com.itene.scalibur.models.DrivingPath;
import com.itene.scalibur.models.Route;
import com.itene.scalibur.models.Waypoint;

import org.json.JSONException;
import org.json.JSONObject;



public class WaypointListAdapter extends RecyclerView.Adapter<WaypointListAdapter.WaypointListHolder> {
    private static final String TAG = com.itene.scalibur.custom.WaypointListAdapter.class.getSimpleName();
    Route route;
    RouteMapActivity context;

    public WaypointListAdapter(Route route, RouteMapActivity activity) {
        this.route = route;
        this.context = activity;
    }

    @Override
    public WaypointListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.waypoint_card, parent, false);
        WaypointListHolder pvh = new WaypointListHolder(v);

        // PICK CONTAINER LISTENER
        pvh.pick_container_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Waypoint container = route.getWaypoints().get(pvh.getAdapterPosition());
                JSONObject post_params = new JSONObject();

                try {
                    post_params.put("status", Waypoint.StatusEnum.PICKED);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String api_url =  String.format(Config.API_POST_ROUTE_CONTAINER, route.getId(), container.getId());
                String api_token = context.getSharedPreferences("com.itene.scalibur", Context.MODE_PRIVATE).getString("API_TOKEN", null);

                VolleyUtils.POST_JSON(context, api_url, post_params, api_token, new VolleyUtils.VolleyJsonResponseListener() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(context, "Can not send route data to the platform", Toast.LENGTH_SHORT).show();
                        Log.e("API", "Can not send route data to the platform, error: " + message);
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("API", "Data uploaded to the platform: " + response.toString());

                            // Delete current path polyline, sets distance and duration to null
                            //route.getCurrent_path().reset();
                            // Get waypoint we are currently showing in the recycler view
                            route.pick_container(container);
                            // Move recycler view to the next destination
                            context.setCurrentItem(route.getWaypoints().indexOf(route.getCurrentDestination()), false);
                            // Notify adapter data has change so it redraws
                            notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        // SKIP CONTAINER LISTENER
        pvh.skip_container_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Waypoint container = route.getWaypoints().get(pvh.getAdapterPosition());
                JSONObject post_params = new JSONObject();

                try {
                    post_params.put("status", Waypoint.StatusEnum.SKIPPED);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String api_url =  String.format(Config.API_POST_ROUTE_CONTAINER, route.getId(), container.getId());
                String api_token = context.getSharedPreferences("com.itene.scalibur", Context.MODE_PRIVATE).getString("API_TOKEN", null);

                VolleyUtils.POST_JSON(context, api_url, post_params, api_token, new VolleyUtils.VolleyJsonResponseListener() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(context, "Can not send status to the platform", Toast.LENGTH_SHORT).show();
                        Log.e("API", "Can not send status to the platform, error: " + message);
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("API", "New status SKIPPED uploaded to the platform: " + response.toString());

                            // Get waypoint we are currently showing in the recycler view
                            route.skip_container(container);
                            // Move recycler view to the next destination
                            context.setCurrentItem(route.getWaypoints().indexOf(route.getCurrentDestination()), false);
                            // Notify adapter data has change so it redraws
                            notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        // UNDO CONTAINER LISTENER
        pvh.undo_container_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Waypoint container = route.getWaypoints().get(pvh.getAdapterPosition());
                JSONObject post_params = new JSONObject();

                try {
                    post_params.put("status", Waypoint.StatusEnum.WAITING);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String api_url =  String.format(Config.API_POST_ROUTE_CONTAINER, route.getId(), container.getId());
                String api_token = context.getSharedPreferences("com.itene.scalibur", Context.MODE_PRIVATE).getString("API_TOKEN", null);

                VolleyUtils.POST_JSON(context, api_url, post_params, api_token, new VolleyUtils.VolleyJsonResponseListener() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(context, "Can not send status to the platform", Toast.LENGTH_SHORT).show();
                        Log.e("API", "Can not send status to the platform, error: " + message);
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("API", "New status WAITING uploaded to the platform: " + response.toString());
                            // Delete current path polyline, sets distance and duration to null
                            //route.getCurrent_path().reset();
                            // Get waypoint we are currently showing in the recycler view
                            route.undo_container(container);
                            // Move recycler view to the next destination
                            context.setCurrentItem(route.getWaypoints().indexOf(route.getCurrentDestination()), false);
                            // Notify adapter data has change so it redraws
                            notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


        return pvh;
    }

    @Override
    public void onBindViewHolder(WaypointListHolder holder, int position) {
        Log.d(TAG, String.format("onBindViewHolder pos: %d", position));
        Waypoint waypoint = route.getWaypoints().get(position);
        DrivingPath path = route.getCurrent_path();

        // Set gone by default
        holder.pick_container_btn.setVisibility(View.GONE);
        holder.skip_container_btn.setVisibility(View.GONE);
        holder.undo_container_btn.setVisibility(View.GONE);
        holder.next_tv.setVisibility(View.GONE);
        holder.picked_tv.setVisibility(View.GONE);
        holder.waiting_tv.setVisibility(View.GONE);
        holder.skipped_tv.setVisibility(View.GONE);
        holder.previous.setVisibility(View.GONE);
        holder.next.setVisibility(View.GONE);
        holder.metrics_ll.setVisibility(View.GONE);

        holder.waypointName.setText(waypoint.getPrettyName());

        if (path != null) {
                holder.distance_tv.setText(path.getPrettyDistance());
                holder.duration_tv.setText(path.getPrettyDuration());
        }

        if (position > 0) { // It´s first element
            holder.previous.setVisibility(View.VISIBLE);
        }

        // It´s not last element
        if (position < route.getWaypoints().size() - 1) {
            holder.next.setVisibility(View.VISIBLE);
        }

        // Drawing the current destination
        if (( route.getCurrentDestination() !=null ) && (route.getWaypoints().indexOf(route.getCurrentDestination()) == position )) {
            holder.next_tv.setVisibility(View.VISIBLE);
            holder.metrics_ll.setVisibility(View.VISIBLE);
            if (route.getCurrentDestination().couldBePicked()) {
                holder.pick_container_btn.setVisibility(View.VISIBLE);
            }
        // It´s not the current destination
        } else {
            // Item is picked
            if (waypoint.isPicked()) {
                holder.picked_tv.setVisibility(View.VISIBLE);
                holder.undo_container_btn.setVisibility(View.VISIBLE);
            } else if (waypoint.isSkipped()) {
                holder.skipped_tv.setVisibility(View.VISIBLE);
                holder.undo_container_btn.setVisibility(View.VISIBLE);
            } else if (route.getCurrentDestination() != null) {
                Integer stops = position - route.getWaypoints().indexOf(route.getCurrentDestination());
                if (stops > 0) {
                    holder.waiting_tv.setText(String.format("%d stop(s) left", stops));
                    holder.waiting_tv.setVisibility(View.VISIBLE);
                }
            }
        }
        // If pickable, is not picked, and is not already skipped  then it can be skipped
        if (waypoint.couldBePicked()) {
            holder.skip_container_btn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return route.getWaypoints().size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class WaypointListHolder extends RecyclerView.ViewHolder {
        TextView waypointName;
        ImageView previous;
        ImageView next;
        TextView next_tv;
        TextView picked_tv;
        TextView waiting_tv;
        TextView skipped_tv;
        LinearLayout metrics_ll;
        TextView distance_tv;
        TextView duration_tv;
        Button pick_container_btn;
        Button skip_container_btn;
        Button undo_container_btn;

        WaypointListHolder(View itemView) {
            super(itemView);
            waypointName = (TextView)itemView.findViewById(R.id.waypoint_name);
            previous = (ImageView)itemView.findViewById(R.id.previous);
            next = (ImageView)itemView.findViewById(R.id.next);
            next_tv = (TextView)itemView.findViewById(R.id.next_tv);
            picked_tv = (TextView)itemView.findViewById(R.id.picked_tv);
            waiting_tv = (TextView)itemView.findViewById(R.id.waiting_tv);
            skipped_tv = (TextView)itemView.findViewById(R.id.skipped_tv);
            metrics_ll = (LinearLayout)itemView.findViewById(R.id.metrics_ll);
            duration_tv = (TextView)itemView.findViewById(R.id.duration_tv);
            distance_tv = (TextView)itemView.findViewById(R.id.distance_tv);
            pick_container_btn = (Button)itemView.findViewById(R.id.pick_container_btn);
            skip_container_btn = (Button)itemView.findViewById(R.id.skip_container_btn);
            undo_container_btn = (Button)itemView.findViewById(R.id.undo_container_btn);

        }
    }

}