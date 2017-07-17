package com.example.key.my_carpathians.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.activities.ActionActivity_;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.PLACE_LIST;
import static com.example.key.my_carpathians.adapters.PlacesRecyclerAdapter.ROUTS_LIST;
import static com.example.key.my_carpathians.adapters.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;

/**
 * Created by Key on 06.07.2017.
 */

public class RoutsRecyclerAdapter extends RecyclerView.Adapter<RoutsRecyclerAdapter.RoutsViewHolder> {
    private List<Rout> routs;
    private List<Place> places;

    /**
     * use context to intent Url
     */
    public Context context;




    public RoutsRecyclerAdapter(List<Rout> routList, List<Place> placeList) {
        this.routs = routList;
        this.places = placeList;
    }

    public static class RoutsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public static final String PUT_EXTRA_ROUT = "routName";
        public Button buttonTypeAndLevel;
        public TextView textNameRout;
        private Rout mRout;
        private  ClickListener mClickListener;


        public RoutsViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mClickListener = listener;
            buttonTypeAndLevel = (Button) itemView.findViewById(R.id.buttonTypeAndLevel);
            textNameRout = (TextView)itemView.findViewById(R.id.textNameRout);
            mRout = null;
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            mClickListener.onPressed(mRout);
        }
        public interface ClickListener {
            void onPressed(Rout nameRout);
        }
    }

    @Override
    public RoutsRecyclerAdapter.RoutsViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lis_item_for_rout, parent, false);
        context = mView.getContext();

        RoutsViewHolder mHolder = new RoutsViewHolder(mView, new RoutsViewHolder.ClickListener() {
            @Override
            public void onPressed(Rout RoutsName) {
                if (RoutsName != null){
                    Intent intent = new Intent(context, ActionActivity_.class);
                    intent.putExtra(PUT_EXTRA_ROUT, RoutsName);
                    ArrayList<Place> arrayListPlace = (ArrayList<Place>)places ;
                    ArrayList<Rout> arrayListRouts = (ArrayList<Rout>) routs;
                    intent.putExtra(PLACE_LIST, arrayListPlace);
                    intent.putExtra(ROUTS_LIST, arrayListRouts);
                    context.startActivity(intent);
                }
            }

        });

        return mHolder;
    }

    @Override
    public void onBindViewHolder(RoutsViewHolder holder, int position) {
        holder.mRout = routs.get(position);
        holder.textNameRout.setText(holder.mRout.getNameRout());
        holder.buttonTypeAndLevel.setText(Integer.toString(holder.mRout.getRoutsLevel()));

    }

    @Override
    public int getItemCount() {
        return routs.size();
    }

}
