package com.example.key.my_carpathians.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.Communicator;
import com.example.key.my_carpathians.models.Place;

import java.util.List;

import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.PLACE;


/**
 * Created by Key on 10.06.2017.
 */

public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder> {
    private List<Place> places;

    /**
     * use context to intent Url
     */
    public Context context;

    public PlacesRecyclerAdapter(List<Place> placeList) {
            this.places = placeList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final static String PUT_EXTRA_PLACE = "placeName";
        public ImageView placeImage;
        public TextView textName;
        public Place mPlace;
        private  ClickListener mClickListener;


        public ViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mClickListener = listener;
            placeImage = (ImageView)itemView.findViewById(R.id.imagePlace);
            textName = (TextView)itemView.findViewById(R.id.textNamePlace);
            mPlace = null;

            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            mClickListener.onPressed(mPlace);
        }
        public interface ClickListener {
            void onPressed(Place namePlace);
        }
    }

    @Override
    public PlacesRecyclerAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_for_place, parent, false);
        context = mView.getContext();

        ViewHolder mHolder = new ViewHolder(mView, new ViewHolder.ClickListener() {
            @Override
            public void onPressed(Place placeName) {
                if (placeName != null){
                    Communicator communicator = (Communicator)context;
                    communicator.putStringNamePlace(placeName.getNamePlace(), PLACE);

                }
            }

        });

        return mHolder;
    }

    @Override
    public void onBindViewHolder(PlacesRecyclerAdapter.ViewHolder holder, int position) {

            holder.mPlace = places.get(position);
            holder.textName.setText(holder.mPlace.getNamePlace());
            Glide
                    .with(context)
                    .load("file:/storage/sdcard0/Android/data/com.example.key.my_carpathians/files/Download/Photos/" + holder.mPlace.getNamePlace())
                    .into(holder.placeImage);

    }

    @Override
    public int getItemCount() {
        return places.size();
    }



}
