package com.example.key.my_carpathians.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorActionActivity;
import com.example.key.my_carpathians.models.Place;
import com.example.key.my_carpathians.models.Rout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class AroundObjectListAdapter extends RecyclerView.Adapter<AroundObjectListAdapter.ViewHolder> {
	public Context context;
	private List<Rout> routs;
	private List<Place> places;


	public AroundObjectListAdapter(List<Place> placeList, List<Rout> routList) {
		this.places = placeList;
		this.routs = routList;
	}

	static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		CheckBox checkBox;
		TextView textNameRout;
		RatingBar ratingBar;
		TextView textLengthTrack;
		Button buttonTypeAndLevel;
		private Rout mRout;
		private Place mPlace;
		private  ClickListener mClickListener;


		ViewHolder(View itemView, ClickListener listener) {
			super(itemView);
			mClickListener = listener;
			checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
			checkBox.setVisibility(View.VISIBLE);
			checkBox.setClickable(false);
			textNameRout = (TextView) itemView.findViewById(R.id.textNameRout);
			textNameRout.setVisibility(View.GONE);
			textLengthTrack = (TextView)itemView.findViewById(R.id.textLenghtTrack);
			ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBarSmall);
			buttonTypeAndLevel =  (Button)itemView.findViewById(R.id.buttonTypeAndLevel);
			mRout = null;
			mPlace = null;
			itemView.setOnClickListener(this);
		}
		@Override
		public void onClick(View v) {
			if (checkBox.isChecked()){
				checkBox.setChecked(false);
			}else {
				checkBox.setChecked(true);
			}
			mClickListener.onPressed(mRout, mPlace);
		}
		 interface ClickListener {
			void onPressed(Rout rout, Place place);
		}
	}

	@Override
	public AroundObjectListAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
		View mView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.lis_item_for_rout, parent, false);
		context = mView.getContext();

		return new ViewHolder(mView, new ViewHolder.ClickListener() {
			@Override
			public void onPressed(Rout rout, Place place) {
					CommunicatorActionActivity communicatorActionActivity = (CommunicatorActionActivity)context;
					communicatorActionActivity.addToMap(rout, place);

			}

		});
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (routs != null) {
			holder.mRout = routs.get(position);
			holder.checkBox.setText(holder.mRout.getNameRout());
			holder.textLengthTrack.setText(holder.mRout.getLengthRout() + "km");
			ratingRout(holder.mRout.getNameRout(), holder.ratingBar);

		}else if (places != null){
			holder.mPlace = places.get(position);
			holder.checkBox.setText(holder.mPlace.getNamePlace());
			holder.textLengthTrack.setVisibility(View.GONE);
			holder.buttonTypeAndLevel.setVisibility(View.GONE);
			ratingPlace(holder.mPlace.getNamePlace(), holder.ratingBar);
		}
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
				if (sum > 0) {
					float averageValue = sum / counter;
					ratingBar.setRating(averageValue);
				}else {
					ratingBar.setRating(0);
				}

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				ratingBar.setRating(0);
			}
		});
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
				if (sum != 0) {
					float averageValue = sum / counter;
					ratingBar.setRating(averageValue);
				}else {
					ratingBar.setRating(0);
				}

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				ratingBar.setRating(0);
			}
		});

	}

	@Override
	public int getItemCount() {
		if (routs == null) {
			return places.size();
		}else {
			return routs.size();
		}
	}

}
