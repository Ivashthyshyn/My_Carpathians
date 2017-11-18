package com.example.key.my_carpathians.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorStartActivity;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.utils.ToolbarActionModeCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.key.my_carpathians.activities.StartActivity.PLACE;
import static com.example.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.example.key.my_carpathians.activities.StartActivity.ROOT_PATH;


/**
 * Created by Key on 10.06.2017.
 */

public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.ViewHolder> {
    private List<Place> places;
    private int mMode;
	SparseBooleanArray mSelectedItemsIds;
    /**
     * use context to intent Url
     */
    public Context context;
	private ActionMode mActionMode;

	public PlacesRecyclerAdapter(List<Place> placeList, int mode) {
            this.places = placeList;
            this.mMode = mode;
	    mSelectedItemsIds = new SparseBooleanArray();
    }
	public void setList(List<Place> placeList, int mode){
		this.places = placeList;
        this.mMode = mode;
        notifyDataSetChanged();
	}
	public void setFilter(List<Place> placeList){
		this.places = placeList;
		notifyDataSetChanged();
	}


	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public final static String PUT_EXTRA_PLACE = "placeName";
        public ImageView placeImage;
        public TextView textName;
        public RatingBar ratingBar;
        private  ClickListener mClickListener;
        private Place mPlace;
	    private int mPosition;

        public ViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mClickListener = listener;
            ratingBar = (RatingBar)itemView.findViewById(R.id.ratingBarForPlaceList);
            placeImage = (ImageView)itemView.findViewById(R.id.imagePlace);
            textName = (TextView)itemView.findViewById(R.id.textNamePlace);
            mPlace = null;
	        mPosition = 0;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }


        @Override
        public void onClick(View v) {
            mClickListener.onPressed(mPosition, mPlace);
        }

        @Override
        public boolean onLongClick(View view) {
            mClickListener.onLongPressed(mPosition, mPlace, view);
            return true;
        }

        public interface ClickListener {
            void onPressed(int position, Place namePlace);

            void onLongPressed(int position, Place mPlace, View view);
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
            public void onPressed(int position, Place placeName) {
                if (mActionMode == null){
                    CommunicatorStartActivity communicatorStartActivity = (CommunicatorStartActivity)context;
                    communicatorStartActivity.putStringNamePlace(placeName);

                }else {
	                onListItemSelect(position);
                }
            }

            @Override
            public void onLongPressed(int position, Place mPlace, View view) {
                if (mMode > 1 && mActionMode == null) {
	                onListItemSelect(position);
                }
            }

        });

        return mHolder;
    }


		@Override
    public void onBindViewHolder(PlacesRecyclerAdapter.ViewHolder holder, int position) {
			holder.mPosition = position;
            holder.mPlace = places.get(position);
            holder.textName.setText(holder.mPlace.getNamePlace());
            ratingPlace(holder.mPlace.getNamePlace(), holder.ratingBar);
			String mRootPath = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(ROOT_PATH, null);
			if (mRootPath != null) {
				Uri root = Uri.parse(mRootPath);
				Glide
						.with(context)
						.load(root.buildUpon().appendPath("Photos").appendPath(holder.mPlace.getNamePlace()).build())
						.into(holder.placeImage);
			}
	    /** Change background color of the selected items in list view  **/
	    holder.itemView
			    .setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4
					    : Color.TRANSPARENT);

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
	private void onListItemSelect(int position) {
		toggleSelection(position);//Toggle the selection

		boolean hasCheckedItems = getSelectedCount() > 0;//Check if any items are already selected or not


		if (hasCheckedItems && mActionMode == null)
			// there are some selected items, start the actionMode
			mActionMode = ((AppCompatActivity) context).startSupportActionMode(new ToolbarActionModeCallback(context,this, null,places, null , mMode));
		else if (!hasCheckedItems && mActionMode != null)
			// there no selected items, finish the actionMode
			mActionMode.finish();

		if (mActionMode != null)
			//set action mode title on item selection
			mActionMode.setTitle(String.valueOf(getSelectedCount()) + " selected");


	}
	public void deletePlaceFromCreated() {
		SparseBooleanArray selected = getSelectedIds();//Get selected ids
		List<String>deletedPlace = new ArrayList<>();
		//Loop all selected ids
		for (int i = (selected.size() - 1); i >= 0; i--) {
			if (selected.valueAt(i)) {
				//If current id is selected remove the item via key
				deletedPlace.add(places.get(i).getNamePlace());
				places.remove(selected.keyAt(i));
				notifyDataSetChanged();//notify adapter
			}
		}
		CommunicatorStartActivity communicatorStartActivity = (CommunicatorStartActivity)context;
		communicatorStartActivity.deletedFromCreatedList(deletedPlace, PLACE);
		Toast.makeText(context, selected.size() + " item deleted.", Toast.LENGTH_SHORT).show();//Show Toast
		mActionMode.finish();
		setNullToActionMode();//Finish action mode after use

	}
	//Delete selected rows
	public void deletePlaceFromFavorit() {
		SparseBooleanArray selected = getSelectedIds();//Get selected ids
		List<String>deletedPlace = new ArrayList<>();
		//Loop all selected ids
		for (int i = (selected.size() - 1); i >= 0; i--) {
			if (selected.valueAt(i)) {
				//If current id is selected remove the item via key
				deletedPlace.add(places.get(i).getNamePlace());
				places.remove(selected.keyAt(i));
				notifyDataSetChanged();//notify adapter
			}
		}
		CommunicatorStartActivity communicatorStartActivity = (CommunicatorStartActivity)context;
		communicatorStartActivity.deletedFromFavoriteList(deletedPlace, PLACE);
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

	//Put or delete selected position into SparseBooleanArray
	public void selectView(int position, boolean value) {
		if (value)
			mSelectedItemsIds.put(position, value);
		else
			mSelectedItemsIds.delete(position);

		notifyDataSetChanged();
	}

    @Override
    public int getItemCount() {
        return places.size();
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
