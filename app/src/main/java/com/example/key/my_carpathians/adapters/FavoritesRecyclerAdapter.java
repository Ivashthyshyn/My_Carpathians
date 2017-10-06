package com.example.key.my_carpathians.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.key.my_carpathians.R;
import com.example.key.my_carpathians.interfaces.CommunicatorStartActivity;

import java.util.List;


public class FavoritesRecyclerAdapter extends RecyclerView.Adapter<FavoritesRecyclerAdapter.FavoritesViewHolder>{
	public CommunicatorStartActivity comunicator;
	public Activity activity;
	public static final int PLACE = 1;
	public static final int ROUT = 2;
	public static final int MY_ROUT = 3;
	public static final int MY_PLACE = 4;
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
				comunicator = (CommunicatorStartActivity)context;
				if (type == PLACE) {
					comunicator.putStringNamePlace(name, type);
				}else if(type == MY_PLACE){
					comunicator.putStringNamePlace(name, type);
				}else if (type == ROUT){
					comunicator.putStringNameRout(name, type);
				}else if (type == MY_ROUT){
					comunicator.putStringNameRout(name, type);
				}
			}

			@Override
			public void onLongPressed(String name) {
				comunicator = (CommunicatorStartActivity)context;
				if (type == PLACE) {
					comunicator.deletedFromFavoriteList(name, type);
				}else if(type == MY_PLACE){
					comunicator.deletedFromCreatedList(name, type);
				}else if (type == ROUT){
					comunicator.deletedFromFavoriteList(name, type);
				}else if (type == MY_ROUT){
					comunicator.deletedFromCreatedList(name, type);
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

	static class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		TextView textName;
		private ClickListener mClickListener;

		FavoritesViewHolder(View itemView, ClickListener listener) {
			super(itemView);
			mClickListener = listener;
			textName = (TextView) itemView.findViewById(R.id.textViewFavoritName);
			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View v) {
			mClickListener.onPressed(textName.getText().toString());

		}

		@Override
		public boolean onLongClick(View view) {
			mClickListener.onLongPressed(textName.getText().toString());
			return false;
		}


		interface ClickListener {
			void onPressed(String name);
			void onLongPressed(String name);
		}
	}
}
