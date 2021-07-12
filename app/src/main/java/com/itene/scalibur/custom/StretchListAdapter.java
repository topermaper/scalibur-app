package com.itene.scalibur.custom;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.itene.scalibur.R;
import com.itene.scalibur.models.Stretch;

import java.util.List;

public class StretchListAdapter extends RecyclerView.Adapter<StretchListAdapter.MyViewHolder> {

    public interface CustomItemClickListener  {
        public void onItemClick(View v, int position);
    }

    private List<Stretch> stretchList;
    private CustomItemClickListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView rv_from, rv_to, rv_estimated;
        public RelativeLayout viewBackground;
        public LinearLayout viewForeground;
        //public ImageView rv_color;

        public MyViewHolder(View view) {
            super(view);
            //rv_color = (ImageView) view.findViewById(R.id.mr_color);
            rv_from = (TextView) view.findViewById(R.id.from_row);
            rv_to = (TextView) view.findViewById(R.id.to_row);
            rv_estimated = (TextView) view.findViewById(R.id.estimated_row);

        }
    }

    /*public ConfigurationListAdapter(List<DRConfiguration> stretchList) {
        this.stretchList = stretchList;
    }*/

    public StretchListAdapter(List<Stretch> stretchList, CustomItemClickListener listener) {
        this.stretchList = stretchList;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.route_row, parent, false);

        final MyViewHolder mViewHolder = new MyViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, mViewHolder.getPosition());
            }
        });

        return mViewHolder;
        //return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Stretch stretch = stretchList.get(position);
        holder.rv_from.setText(String.format("From: %s", stretch.getFrom_name()));
        holder.rv_to.setText(String.format("To: %s", stretch.getTo_name()));
        holder.rv_estimated.setText(String.format("Estimated(%.0f s, %.0f m)", stretch.getTime(), stretch.getDistance()));
    }

    @Override
    public int getItemCount() {
        return stretchList.size();
    }

    public void removeItem(int position) {
        stretchList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void removeAll() {
        int n = getItemCount();
        stretchList.clear();
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRangeRemoved(0, n);
    }

    public void restoreItem(Stretch item, int position) {
        stretchList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }
}
