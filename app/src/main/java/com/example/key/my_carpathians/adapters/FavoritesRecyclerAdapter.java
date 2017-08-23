package com.example.key.my_carpathians.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.activities.MapsActivity_;
import com.example.key.my_carpathians.interfaces.Comunicator;

import java.util.ArrayList;
import java.util.List;

import static com.example.key.my_carpathians.activities.ActionActivity.SELECTED_USER_ROUTS;


public class FavoritesRecyclerAdapter extends RecyclerView.Adapter<FavoritesRecyclerAdapter.FavoritesViewHolder>{
	public Comunicator comunicator;
	public Activity activity;
	public static final int PLACE = 1;
	public static final int ROUT = 2;
	public static final int MY_ROUT = 3;
	public Context context;
	public int type;
	private List<String> stringList;



	public FavoritesRecyclerAdapter(Activity activity, List<String> stringList, int type) {
		this.stringList = stringList;
		this.type = type;
		this.activity = activity;
	}

	@Override
	public FavoritesViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
		View mView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.list_item_for_favorites, parent, false);
		context = mView.getContext();

		FavoritesViewHolder mHolder = new FavoritesViewHolder(mView, new FavoritesViewHolder.ClickListener() {
			@Override
			public void onPressed(String name) {
				comunicator = (Comunicator)context;
				if (type == PLACE) {
					comunicator.putStringNamePlace(name, type);
				}else if (type == ROUT){
					comunicator.putStringNameRout(name, type);
				}else if (type == MY_ROUT){

					ArrayList<String> selectedUserTrack = new ArrayList<>();
					selectedUserTrack.add(name);
					Intent mapsActivityIntent = new Intent(context, MapsActivity_.class);
					mapsActivityIntent.putStringArrayListExtra(SELECTED_USER_ROUTS, selectedUserTrack);
					context.startActivity(mapsActivityIntent);
				}
			}

		});

		return mHolder;
	}

	@Override
	public void onBindViewHolder(FavoritesViewHolder holder, int position) {
			holder.textName.setText(stringList.get(position));

	}


	@Override
	public int getItemCount() {
		return stringList.size();
	}

	static class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView textName;
		private ClickListener mClickListener;


		FavoritesViewHolder(View itemView, ClickListener listener) {
			super(itemView);
			mClickListener = listener;
			textName = (TextView) itemView.findViewById(R.id.textViewFavoritName);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			mClickListener.onPressed(textName.getText().toString());
		}


		interface ClickListener {
			void onPressed(String name);
		}
	}
}
