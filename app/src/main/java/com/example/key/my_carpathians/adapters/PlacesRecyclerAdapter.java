package com.example.key.my_carpathians.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.activities.PlaceActivity_;
import com.example.key.my_carpathians.models.Place;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLASE;


/**
 * Created by Key on 10.06.2017.
 */

public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder> {
    private List<Place> list;

    /**
     * use context to intent Url
     */
    public Context context;




    public PlacesRecyclerAdapter(List<Place> list) {
            this.list = list;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final static String PUT_EXTRA_PLASE = "placeName";
        public ImageView placeImage;
        public TextView textName;
        private  ClickListener mClickListener;


        public ViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mClickListener = listener;
            placeImage = (ImageView)itemView.findViewById(R.id.imagePlace);
            textName = (TextView)itemView.findViewById(R.id.textNamePlace);

            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            mClickListener.onPressed(textName.getText().toString());
        }
        public interface ClickListener {
            void onPressed(String namePlace);
        }
    }

    @Override
    public PlacesRecyclerAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        context = mView.getContext();

        ViewHolder mHolder = new ViewHolder(mView, new ViewHolder.ClickListener() {
            @Override
            public void onPressed(String placeName) {
                if (placeName != null){
                    Intent intent = new Intent(context, PlaceActivity_.class);
                    intent.putExtra(PUT_EXTRA_PLASE, placeName);
                    ArrayList<Place> arrayListPlace = (ArrayList<Place>) list;
                    intent.putExtra("fdfd", arrayListPlace);
                    context.startActivity(intent);
                }
            }

        });

        return mHolder;
    }

    @Override
    public void onBindViewHolder(PlacesRecyclerAdapter.ViewHolder holder, int position) {

            Place mPlace = list.get(position);
            holder.textName.setText(mPlace.getNamePlace());
            Glide
                    .with(context)
                    .load(mPlace.getUrlPlace())
                    .into(holder.placeImage);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }



}
