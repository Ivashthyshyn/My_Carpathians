package com.example.key.my_carpathians.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorStartActivity;
import com.example.key.my_carpathians.models.Rout;
import com.example.key.my_carpathians.utils.ToolbarActionModeCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.activities.StartActivity.ROUT;
import static com.example.key.my_carpathians.fragments.EditModeFragment.HARD;
import static com.example.key.my_carpathians.fragments.EditModeFragment.LIGHT;
import static com.example.key.my_carpathians.fragments.EditModeFragment.MEDIUM;

/**
 *
 */

public class RoutsRecyclerAdapter extends RecyclerView.Adapter<RoutsRecyclerAdapter.RoutsViewHolder> {
    public static final String PUT_EXTRA_POINTS = "put_extra_point_list";
    SparseBooleanArray mSelectedItemsIds;
    private int mMode;
    public Context context;
    private List<Rout> routs;
    private ActionMode mActionMode;


    public RoutsRecyclerAdapter(List<Rout> routList, int mode) {
        this.routs = routList;
        this.mMode = mode;
        mSelectedItemsIds = new SparseBooleanArray();
    }
	public void setList(List<Rout> routList, int mode){
		this.routs = routList;
        this.mMode = mode;
        notifyDataSetChanged();
	}

    @Override
    public RoutsRecyclerAdapter.RoutsViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lis_item_for_rout, parent, false);
        context = mView.getContext();

        RoutsViewHolder mHolder = new RoutsViewHolder(mView, new RoutsViewHolder.ClickListener() {
            @Override
            public void onPressed(int position, Rout routObject) {
                if (mActionMode == null && routObject != null) {
                    CommunicatorStartActivity communicatorStartActivity = (CommunicatorStartActivity)context;
                    communicatorStartActivity.putStringNameRout(routObject);
                }else{
                    onListItemSelect(position);
                }
            }

            @Override
            public void onLongPressed(int position, Rout mRout, View view) {
                if (mMode > 1  && mActionMode == null){
                    onListItemSelect(position);
                }
            }

        });

        return mHolder;
    }

    @Override
    public void onBindViewHolder(RoutsViewHolder holder, int position) {
        holder.mPosition = position;
        holder.mRout = routs.get(position);
        holder.textNameRout.setText(holder.mRout.getNameRout());
        holder.textLengthTrack.setText(holder.mRout.getLengthRout() + "km");
        ratingRout(holder.mRout.getNameRout(), holder.ratingBar);
        switch (holder.mRout.getRoutsLevel()) {
            case LIGHT:
                holder.buttonTypeAndLevel.setBackgroundResource(R.color.color_level_green);
                return;
            case MEDIUM:
                holder.buttonTypeAndLevel.setBackgroundResource(R.color.color_level_yellow);
                return;
            case HARD:
                holder.buttonTypeAndLevel.setBackgroundResource(R.color.color_level_red);
        }
        holder.itemView
                .setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4
                        : Color.TRANSPARENT);
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



    public static class RoutsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public static final String PUT_EXTRA_ROUT = "routName";
        private int mPosition;
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
            mPosition = 0;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }


        @Override
        public void onClick(View v) {
            mClickListener.onPressed(mPosition, mRout);
        }

        @Override
        public boolean onLongClick(View view) {
            mClickListener.onLongPressed(mPosition, mRout, view);
            return true;
        }

        interface ClickListener {
            void onPressed(int position, Rout nameRout);

            void onLongPressed(int position, Rout mRout, View view);
        }
    }

    private void onListItemSelect(int position) {
        toggleSelection(position);//Toggle the selection

        boolean hasCheckedItems = getSelectedCount() > 0;//Check if any items are already selected or not


        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity) context).startSupportActionMode(new ToolbarActionModeCallback(context,null, this,null, routs , mMode));
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(getSelectedCount()) + " selected");


    }
    public void deleteRoutFromCreated() {
        SparseBooleanArray selected = getSelectedIds();//Get selected ids
        List<String>deletedRouts = new ArrayList<>();
        //Loop all selected ids
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)) {
                //If current id is selected remove the item via key
                deletedRouts.add(routs.get(i).getNameRout());
                routs.remove(selected.keyAt(i));
                notifyDataSetChanged();//notify adapter
            }
        }
        CommunicatorStartActivity communicatorStartActivity = (CommunicatorStartActivity)context;
        communicatorStartActivity.deletedFromCreatedList(deletedRouts, ROUT);
        Toast.makeText(context, selected.size() + " item deleted.", Toast.LENGTH_SHORT).show();//Show Toast
        mActionMode.finish();//Finish action mode after use

    }
    //Delete selected rows
    public void deleteRoutFromFavorit() {
        SparseBooleanArray selected = getSelectedIds();//Get selected ids
        List<String>deletedRouts = new ArrayList<>();
        //Loop all selected ids
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)) {
                //If current id is selected remove the item via key
                deletedRouts.add(routs.get(i).getNameRout());
                routs.remove(selected.keyAt(i));
                notifyDataSetChanged();//notify adapter
            }
        }
        CommunicatorStartActivity communicatorStartActivity = (CommunicatorStartActivity)context;
        communicatorStartActivity.deletedFromFavoriteList(deletedRouts, ROUT);
        Toast.makeText(context, selected.size() + " item deleted.", Toast.LENGTH_SHORT).show();//Show Toast
        mActionMode.finish();//Finish action mode after use

    }
    //Remove selected selections
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }
    //Set action mode null after use
    public void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode.finish();
            mActionMode = null;
    }
    //Toggle selection methods
    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    //Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }
    //Return all selected ids
    public SparseBooleanArray getSelectedIds() {
            return mSelectedItemsIds;
    }
    public boolean ismMode(){
        if (mActionMode != null){
            return true;
        }else{
            return false;
        }
    }
}
