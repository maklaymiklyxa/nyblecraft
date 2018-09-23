package com.dimas.android.locatr.FavoritePlace;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dimas.android.locatr.DisplayFavList;
import com.dimas.android.locatr.MapActivity;
import com.dimas.android.locatr.R;

import java.util.List;

public class PlaceFavAdapter extends RecyclerView.Adapter<PlaceFavAdapter.CustomViewHolder> {

    private List<PlaceInfo> placeInfos;
    private Context context;
    private PlaceInfo mPlaceInfo;

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView titleView, bodyView, temperature, weather, curDate;
        protected ImageButton shareButton, deleteButton;

        public CustomViewHolder(View view) {
            super(view);
            this.titleView = (TextView) view.findViewById(R.id.info_title);
            this.bodyView = (TextView) view.findViewById(R.id.info_body);
            this.curDate = (TextView) view.findViewById(R.id.date);
            this.shareButton = (ImageButton) view.findViewById(R.id.share_button);
            this.deleteButton = (ImageButton) view.findViewById(R.id.delete_button);

        }
    }

    public PlaceFavAdapter(List<PlaceInfo> placeInfos, Context context) {
        this.placeInfos = placeInfos;
        this.context = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_list_item, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaceInfo != null) {
                    if (mPlaceInfo.getWeather() == null) {
                        new android.app.AlertDialog.Builder(context)
                                .setTitle(mPlaceInfo.getTitle())
                                .setMessage(mPlaceInfo.getBody())
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                })
                                .create()
                                .show();
                    } else {
                        new android.app.AlertDialog.Builder(context)
                                .setTitle(mPlaceInfo.getTitle())
                                .setMessage(mPlaceInfo.getBody() + ", " + mPlaceInfo.getTemperatureToString() + ", " + mPlaceInfo.getWeather())
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                })
                                .create()
                                .show();
                    }
                }
            }
        });
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        final int pos = holder.getAdapterPosition();
        mPlaceInfo = placeInfos.get(position);
        holder.titleView.setText(mPlaceInfo.getTitle());
        holder.bodyView.setText(mPlaceInfo.getBody());
        holder.curDate.setText(mPlaceInfo.getDate().toString());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeInfos.remove(mPlaceInfo);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, placeInfos.size());
                ((DisplayFavList) context).saveArrayList();
            }
        });

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mPlaceInfo.getTitle() + ".\nAddress: " + mPlaceInfo.getBody());
                sendIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, "Share Location Via..."));
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != placeInfos ? placeInfos.size() : 0);
    }
}