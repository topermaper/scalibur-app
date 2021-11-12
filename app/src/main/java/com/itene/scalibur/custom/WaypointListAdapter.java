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

    Route route;
    RouteMapActivity context;

    public WaypointListAdapter(Route route, RouteMapActivity activity){
        this.route = route;
        this.context = activity;
    }

    @Override
    public WaypointListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.waypoint_card, parent, false);
        WaypointListHolder pvh = new WaypointListHolder(v);

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
                            route.getCurrent_path().reset();
                            // Get waypoint we are currently showing in the recycler view
                            route.pick_container(container);
                            // Move recycler view to the next destination
                            context.setCurrentItem(route.getWaypoints().indexOf(route.getCurrentDestination()), true);
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
        Log.d("WaypointListAdapter", String.format("onBindViewHolder pos: %d", position));
        Waypoint waypoint = route.getWaypoints().get(position);
        DrivingPath path = route.getCurrent_path();

        holder.waypointName.setText(waypoint.getPrettyName());

        if (path != null) {
                holder.distance_tv.setText(path.getPrettyDistance());
                holder.duration_tv.setText(path.getPrettyDuration());
        }

        if (position > 0) { // It´s first element
            holder.previous.setVisibility(View.VISIBLE);
        } else { // It´s not first element
            holder.previous.setVisibility(View.GONE);
        }

        // It´s not last element
        if (position < route.getWaypoints().size() - 1) {
            holder.next.setVisibility(View.VISIBLE);
        } else {  // It´s last element
            holder.next.setVisibility(View.GONE);
        }

        // Drawing the current destination
        if (( route.getCurrentDestination() !=null ) && (route.getWaypoints().indexOf(route.getCurrentDestination()) == position )) {
            holder.next_tv.setVisibility(View.VISIBLE);
            holder.metrics_ll.setVisibility(View.VISIBLE);
            if (route.getCurrentDestination().canBePicked()) {
                holder.pick_container_btn.setVisibility(View.VISIBLE);
            } else {
                holder.pick_container_btn.setVisibility(View.GONE);
            }
        // It´s not the current destination
        } else {
            holder.next_tv.setVisibility(View.GONE);
            holder.metrics_ll.setVisibility(View.GONE);
            holder.pick_container_btn.setVisibility(View.GONE);
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
        LinearLayout metrics_ll;
        TextView distance_tv;
        TextView duration_tv;
        Button pick_container_btn;

        WaypointListHolder(View itemView) {
            super(itemView);
            waypointName = (TextView)itemView.findViewById(R.id.waypoint_name);
            previous = (ImageView)itemView.findViewById(R.id.previous);
            next = (ImageView)itemView.findViewById(R.id.next);
            next_tv = (TextView)itemView.findViewById(R.id.next_tv);
            metrics_ll = (LinearLayout)itemView.findViewById(R.id.metrics_ll);
            duration_tv = (TextView)itemView.findViewById(R.id.duration_tv);
            distance_tv = (TextView)itemView.findViewById(R.id.distance_tv);
            pick_container_btn = (Button)itemView.findViewById(R.id.pick_container_btn);

        }
    }

}