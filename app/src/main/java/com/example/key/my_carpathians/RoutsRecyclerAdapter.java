package com.example.key.my_carpathians;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.key.my_carpathians.database.Rout;

import java.util.List;

import static com.example.key.my_carpathians.RoutsRecyclerAdapter.RoutsViewHolder.PUT_EXTRA_ROUT;

/**
 * Created by Key on 06.07.2017.
 */

public class RoutsRecyclerAdapter extends RecyclerView.Adapter<RoutsRecyclerAdapter.RoutsViewHolder> {
    private List<Rout> list;

    /**
     * use context to intent Url
     */
    public Context context;




    public RoutsRecyclerAdapter(List<Rout> list) {
        this.list = list;

    }

    public static class RoutsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public static final String PUT_EXTRA_ROUT = "routName";
        public Button buttonTypeAndLevel;
        public TextView textNameRout;
        private  ClickListener mClickListener;


        public RoutsViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mClickListener = listener;
            buttonTypeAndLevel = (Button) itemView.findViewById(R.id.buttonTypeAndLevel);
            textNameRout = (TextView)itemView.findViewById(R.id.textNameRout);

            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            mClickListener.onPressed(textNameRout.getText().toString());
        }
        public interface ClickListener {
            void onPressed(String nameRout);
        }
    }

    @Override
    public RoutsRecyclerAdapter.RoutsViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lis_item_for_rout, parent, false);
        context = mView.getContext();

        RoutsViewHolder mHolder = new RoutsViewHolder(mView, new RoutsViewHolder.ClickListener() {
            @Override
            public void onPressed(String RoutsName) {
                if (RoutsName != null){
                    Intent intent = new Intent(context, RoutActivity_.class);
                    intent.putExtra(PUT_EXTRA_ROUT, RoutsName);
                    context.startActivity(intent);
                }
            }

        });

        return mHolder;
    }

    @Override
    public void onBindViewHolder(RoutsViewHolder holder, int position) {
        Rout mRout = list.get(position);
        holder.textNameRout.setText(mRout.getNameRout());
        holder.buttonTypeAndLevel.setText(Integer.toString(mRout.getRoutsLevel()));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
