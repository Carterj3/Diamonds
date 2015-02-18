package com.diamonds.app;

import java.util.List;

import com.appspot.diamonds_app.diamonds.model.Game;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GameArrayAdapter extends ArrayAdapter<Game> {

	public GameArrayAdapter(Context context, int resource,
			int textViewResourceId, List<Game> objects) {
		super(context, resource, textViewResourceId, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		TextView nameTextView = (TextView) view.findViewById(android.R.id.text1);
		nameTextView.setText(getItem(position).getName());
		TextView ipTextView = (TextView) view.findViewById(android.R.id.text2);
		ipTextView.setText(getItem(position).getIp());
		return view;
	}

}
