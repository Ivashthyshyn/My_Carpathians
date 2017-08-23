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
import com.example.key.my_carpathians.activities.ActionActivity_;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ViewHolder.PUT_EXTRA_PLACE;


/**
 * Created by Key on 10.06.2017.
 */

public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder> {
    public static final String PUT_EXTRA_PLACE_LIST = "place_list";
    public static final String PUT_EXTRA_ROUTS_LIST = "routs_list";
    private List<Place> places;
    private List<Rout> routs;
    /**
     * use context to intent Url
     */
    public Context context;




    public PlacesRecyclerAdapter(List<Place> placeList, List<Rout> routList ) {
            this.places = placeList;
            this.routs = routList;
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
                    Intent intent = new Intent(context, ActionActivity_.class);
                    intent.putExtra(PUT_EXTRA_PLACE, placeName);
                    ArrayList<Place> arrayListPlace = (ArrayList<Place>) places;
                    ArrayList<Rout> arrayListRouts = (ArrayList<Rout>) routs;
                    intent.putExtra(PUT_EXTRA_PLACE_LIST, arrayListPlace);
                    intent.putExtra(PUT_EXTRA_ROUTS_LIST, arrayListRouts);
                    context.startActivity(intent);
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
