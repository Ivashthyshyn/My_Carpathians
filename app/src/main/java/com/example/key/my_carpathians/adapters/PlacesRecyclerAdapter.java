package com.example.key.my_carpathians.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorStartActivity;
import com.example.key.my_carpathians.models.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.PLACE;


/**
 * Created by Key on 10.06.2017.
 */

public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder> {
    private List<Place> places;
    private boolean mMode;

    /**
     * use context to intent Url
     */
    public Context context;

    public PlacesRecyclerAdapter(List<Place> placeList, boolean mode) {
            this.places = placeList;
            this.mMode = mode;
    }
	public void setList(List<Place> placeList, boolean mode){
		this.places = placeList;
        this.mMode = mode;
	}

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public final static String PUT_EXTRA_PLACE = "placeName";
        public ImageView placeImage;
        public TextView textName;
        public RatingBar ratingBar;
        private  ClickListener mClickListener;
        private Place mPlace;

        public ViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mClickListener = listener;
            ratingBar = (RatingBar)itemView.findViewById(R.id.ratingBarForPlaceList);
            placeImage = (ImageView)itemView.findViewById(R.id.imagePlace);
            textName = (TextView)itemView.findViewById(R.id.textNamePlace);
            mPlace = null;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            mClickListener.onPressed(mPlace);
        }

        @Override
        public boolean onLongClick(View view) {
            mClickListener.onLongPressed(mPlace, view);
            return true;
        }

        public interface ClickListener {
            void onPressed(Place namePlace);

            void onLongPressed(Place mPlace, View view);
        }
    }

    @Override
    public PlacesRecyclerAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_for_place, parent, false);
        context = mView.getContext();
        if (places.size() == 0){
            
        }

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int height = metrics.heightPixels;
        ViewGroup.LayoutParams params = mView.getLayoutParams();
        params.height = (height / 4) - 6;
        mView.setLayoutParams(params);
        ViewHolder mHolder = new ViewHolder(mView, new ViewHolder.ClickListener() {
            @Override
            public void onPressed(Place placeName) {
                if (placeName != null){
                    CommunicatorStartActivity communicatorStartActivity = (CommunicatorStartActivity)context;
                    communicatorStartActivity.putStringNamePlace(placeName.getNamePlace(), PLACE);

                }
            }

            @Override
            public void onLongPressed(Place mPlace, View view) {
                if (mMode){
                    CommunicatorStartActivity communicatorStartActivity = (CommunicatorStartActivity)context;
                    communicatorStartActivity.deletedFromFavoriteList(mPlace.getNamePlace(), PLACE);

                }
            }

        });

        return mHolder;
    }

    @Override
    public void onBindViewHolder(PlacesRecyclerAdapter.ViewHolder holder, int position) {

            holder.mPlace = places.get(position);
            holder.textName.setText(holder.mPlace.getNamePlace());
            ratingPlace(holder.mPlace.getNamePlace(), holder.ratingBar);
            Uri rootPathForTitlePhotoString = Uri.fromFile(context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS)).buildUpon().appendPath("Photos").build();

            Glide
                    .with(context)
                    .load(rootPathForTitlePhotoString.buildUpon().appendPath(holder.mPlace.getNamePlace()).build())
                    .into(holder.placeImage);

    }
    private void ratingPlace(String namePlace, final RatingBar ratingBar) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Query myPlace = myRef.child("Rating").child(namePlace);

        myPlace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int counter = 0;
                float sum = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    counter++;
                    float value = postSnapshot.getValue(float.class);
                    sum = sum + value;

                }
                float averageValue = sum/counter;
                ratingBar.setRating( averageValue);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ratingBar.setRating(0);
            }
        });

    }

    @Override
    public int getItemCount() {
        return places.size();
    }



}
