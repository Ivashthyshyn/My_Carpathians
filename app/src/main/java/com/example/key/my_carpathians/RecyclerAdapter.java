package com.example.key.my_carpathians;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.database.Places;
import com.example.key.my_carpathians.database.Routs;

import java.util.List;

import static com.example.key.my_carpathians.StartActivity.TYPE_OF_LIST_PLACE;
import static com.example.key.my_carpathians.StartActivity.TYPE_OF_LIST_ROUTS;


/**
 * Created by Key on 10.06.2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private int type = 0;
    private List<?> list;

    /**
     * use context to intent Url
     */
    public Context context;




    public RecyclerAdapter(List<?> list,int type) {
            this.list = list;
            this.type = type;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
    public RecyclerAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        context = mView.getContext();

        ViewHolder mHolder = new ViewHolder(mView, new ViewHolder.ClickListener() {
            @Override
            public void onPressed(String placeName) {
                if (placeName != null){
                    Intent intent = new Intent(context, ActionActivity_.class);
                    intent.putExtra("placeName", placeName);
                    context.startActivity(intent);
                }
            }

        });

        return mHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
        if (type == TYPE_OF_LIST_PLACE) {
            List<Places> places = (List<Places>) list;
            Places mPlace = places.get(position);
            holder.textName.setText(mPlace.getNamePlace());
            Glide
                    .with(context)
                    .load(mPlace.getUrlPlace())
                    .into(holder.placeImage);
        }else if (type == TYPE_OF_LIST_ROUTS){
            List<Routs> routs = (List<Routs>) list;
            Routs mRout = routs.get(position);
            holder.textName.setText(mRout.getNameRout());
            Glide
                    .with(context)
                    .load(mRout.getUrlRout())
                    .into(holder.placeImage);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }



}
