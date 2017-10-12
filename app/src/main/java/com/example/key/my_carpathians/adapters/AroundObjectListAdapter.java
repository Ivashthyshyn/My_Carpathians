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
			ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
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

		}else if (places != null){
			holder.mPlace = places.get(position);
			holder.checkBox.setText(holder.mPlace.getNamePlace());
			holder.textLengthTrack.setVisibility(View.GONE);
			holder.buttonTypeAndLevel.setVisibility(View.GONE);

		}
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
