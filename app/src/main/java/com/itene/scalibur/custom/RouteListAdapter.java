package com.itene.scalibur.custom;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.itene.scalibur.R;
import com.itene.scalibur.RouteMapActivity;
import com.itene.scalibur.SelectRouteActivity;
import com.itene.scalibur.models.Route;


import java.util.List;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.RouteListHolder> {

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
                Toast.makeText(context, String.format("Route #%d", route.getId()), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, RouteMapActivity.class);
                // Pass route id to the new activity
                intent.putExtra("route_id", route.getId());
                // Pass logged user
                intent.putExtra("user", (Parcelable)context.getIntent().getParcelableExtra("user"));
                context.startActivity(intent);
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