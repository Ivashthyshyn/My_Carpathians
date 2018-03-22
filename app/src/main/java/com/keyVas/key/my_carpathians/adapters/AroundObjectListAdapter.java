package com.keyVas.key.my_carpathians.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.keyVas.key.my_carpathians.R;
import com.keyVas.key.my_carpathians.interfaces.CommunicatorActionActivity;
import com.keyVas.key.my_carpathians.models.Place;
import com.keyVas.key.my_carpathians.models.Rout;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.keyVas.key.my_carpathians.activities.StartActivity.PREFS_NAME;
import static com.keyVas.key.my_carpathians.activities.StartActivity.USER_LANGUAGE;
import static com.keyVas.key.my_carpathians.fragments.EditModeFragment.HARD;
import static com.keyVas.key.my_carpathians.fragments.EditModeFragment.LIGHT;
import static com.keyVas.key.my_carpathians.fragments.EditModeFragment.MEDIUM;
import static com.keyVas.key.my_carpathians.models.Place.EN;


public class AroundObjectListAdapter extends RecyclerView.Adapter<AroundObjectListAdapter.ViewHolder> {
	public Context context;
	private List<Rout> routs;
	private List<Place> places;
	private String mUserLanguage;


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
			checkBox =  itemView.findViewById(R.id.checkBox);
			checkBox.setVisibility(View.VISIBLE);
			checkBox.setClickable(false);
			textNameRout =  itemView.findViewById(R.id.textNameRout);
			textNameRout.setVisibility(View.GONE);
			textLengthTrack = itemView.findViewById(R.id.textLenghtTrack);
			ratingBar =  itemView.findViewById(R.id.ratingBarSmall);
			buttonTypeAndLevel = itemView.findViewById(R.id.buttonTypeAndLevel);
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
		mUserLanguage = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(USER_LANGUAGE, EN);

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
			holder.checkBox.setText(holder.mRout.getNameRout(mUserLanguage));
			holder.textLengthTrack.setText(holder.mRout.getLengthRout() + ""
					+ context.getResources().getString(R.string.km));
			ratingRout(holder.mRout.routKey(), holder.ratingBar);
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

		}else if (places != null){
			holder.mPlace = places.get(position);
			holder.checkBox.setText(holder.mPlace.getNamePlace(mUserLanguage));
			holder.textLengthTrack.setVisibility(View.GONE);
			holder.buttonTypeAndLevel.setVisibility(View.GONE);
			ratingPlace(holder.mPlace.placeKey(), holder.ratingBar);
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
