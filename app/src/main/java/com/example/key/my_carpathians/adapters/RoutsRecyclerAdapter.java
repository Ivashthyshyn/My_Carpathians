package com.example.key.my_carpathians.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorStartActivity;
import com.example.key.my_carpathians.models.Rout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.example.key.my_carpathians.adapters.FavoritesRecyclerAdapter.ROUT;
import static com.example.key.my_carpathians.fragments.EditModeFragment.HARD;
import static com.example.key.my_carpathians.fragments.EditModeFragment.LIGHT;
import static com.example.key.my_carpathians.fragments.EditModeFragment.MEDIUM;

/**
 *
 */

public class RoutsRecyclerAdapter extends RecyclerView.Adapter<RoutsRecyclerAdapter.RoutsViewHolder> {
    public static final String PUT_EXTRA_POINTS = "put_extra_point_list";
    public Context context;
    private List<Rout> routs;


    public RoutsRecyclerAdapter(List<Rout> routList) {
        this.routs = routList;
    }

    @Override
    public RoutsRecyclerAdapter.RoutsViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lis_item_for_rout, parent, false);
        context = mView.getContext();

        RoutsViewHolder mHolder = new RoutsViewHolder(mView, new RoutsViewHolder.ClickListener() {
            @Override
            public void onPressed(Rout routObject) {
                if (routObject != null) {
                    CommunicatorStartActivity communicatorStartActivity = (CommunicatorStartActivity)context;
                    communicatorStartActivity.putStringNameRout(routObject.getNameRout(), ROUT);
                }
            }

        });

        return mHolder;
    }

    @Override
    public void onBindViewHolder(RoutsViewHolder holder, int position) {
        holder.mRout = routs.get(position);
        holder.textNameRout.setText(holder.mRout.getNameRout());
        holder.textLengthTrack.setText(holder.mRout.getLengthRout() + "km");
        ratingRout(holder.mRout.getNameRout(), holder.ratingBar);
        switch (holder.mRout.getRoutsLevel()) {
            case LIGHT:
                holder.buttonTypeAndLevel.setBackgroundResource(R.color.colorGreenPrimary);
                return;
            case MEDIUM:
                holder.buttonTypeAndLevel.setBackgroundResource(R.color.colorYellowPrimary);
                return;
            case HARD:
                holder.buttonTypeAndLevel.setBackgroundResource(R.color.colorRedPrimary);
        }
    }

    private void ratingRout(String nameRout, final RatingBar ratingBar) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            Query myPlace = myRef.child("Rating").child(nameRout);

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
        return routs.size();
    }

    public static class RoutsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public static final String PUT_EXTRA_ROUT = "routName";
        public Button buttonTypeAndLevel;
        public TextView textNameRout;
        final public TextView textLengthTrack;
        public RatingBar ratingBar;
        private Rout mRout;
        private ClickListener mClickListener;


        RoutsViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mClickListener = listener;
            buttonTypeAndLevel = (Button) itemView.findViewById(R.id.buttonTypeAndLevel);
            textNameRout = (TextView) itemView.findViewById(R.id.textNameRout);
            textLengthTrack = (TextView) itemView.findViewById(R.id.textLenghtTrack);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBarSmall);
            mRout = null;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            mClickListener.onPressed(mRout);
        }

        interface ClickListener {
            void onPressed(Rout nameRout);
        }
    }

}
