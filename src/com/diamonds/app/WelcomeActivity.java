package com.diamonds.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.appspot.diamonds_app.diamonds.Diamonds;
import com.appspot.diamonds_app.diamonds.Diamonds.Game;
import com.appspot.diamonds_app.diamonds.model.*;
import com.diamonds.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class WelcomeActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private EditText mIp;
	private String mUsername;
	public static final String KEY_IP = "IP";
	public static final String KEY_ISHOST = "ISHOST";
	public static final String KEY_ISPUBLIC = "ISPUBLIC";
	
	public static final String KEY_Player1 = "";
	public static final String KEY_Player2 = "";
	public static final String KEY_Player3 = "";
	public static final String KEY_Player4 = "";

	private ListView mListView;
	private ArrayAdapter adapter;
	private Diamonds mService;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_help:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Tutorial");
			alert.setMessage(R.string.tutorial);
			alert.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					});
			alert.show();
			return true;
		case R.id.menu_about:
			AlertDialog.Builder alert1 = new AlertDialog.Builder(this);
			alert1.setTitle("About");
			alert1.setMessage(R.string.about);
			alert1.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					});
			alert1.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		Intent data = getIntent();
		mUsername = data.getStringExtra(LoginActivity.KEY_USERNAME);

		mIp = ((EditText) findViewById(R.id.join_game_edittext));

		((Button) findViewById(R.id.create_public_game_button))
				.setOnClickListener(this);
		((Button) findViewById(R.id.create_private_game_button))
				.setOnClickListener(this);

		((Button) findViewById(R.id.join_game_button)).setOnClickListener(this);
		((Button) findViewById(R.id.lobby_refresh_public_games))
				.setOnClickListener(this);

		mListView = (ListView) findViewById(R.id.lobby_public_game_listview);

		Diamonds.Builder builder = new Diamonds.Builder(
				AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
		builder.setApplicationName("Diamonds-App");
		mService = builder.build();

		mListView.setOnItemClickListener(this);

		updateGames();
	}

	@Override
	public void onClick(View v) {
		String ip = mIp.getText().toString();
		if (v.getId() == R.id.create_public_game_button) {
			joinGame(ip, true, true);
		} else if (v.getId() == R.id.create_private_game_button) {
			joinGame(ip, false, true);
		} else if (ip.length() > 0 && v.getId() == R.id.join_game_button) {
			Intent lobby = new Intent(WelcomeActivity.this, LobbyActivity.class);
			lobby.putExtra(KEY_IP, ip);
			lobby.putExtra(LoginActivity.KEY_USERNAME, mUsername);
			lobby.putExtra(KEY_ISHOST, false);
			startActivity(lobby);
		} else if (v.getId() == R.id.lobby_refresh_public_games) {
			updateGames();
		}

	}

	private void joinGame(String ip, Boolean isPublic, Boolean isHost) {
		Intent lobby = new Intent(WelcomeActivity.this, LobbyActivity.class);
		lobby.putExtra(KEY_IP, ip);
		lobby.putExtra(LoginActivity.KEY_USERNAME, mUsername);
		lobby.putExtra(KEY_ISHOST, isHost);
		lobby.putExtra(KEY_ISPUBLIC, isPublic);
		startActivity(lobby);
	}

	private void updateGames() {
		new QueryForGamesTask().execute();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		com.appspot.diamonds_app.diamonds.model.Game game = (com.appspot.diamonds_app.diamonds.model.Game) adapter
				.getItem(position);

		joinGame(game.getIp(), true, false);

	}

	class QueryForGamesTask extends AsyncTask<Void, Void, GameCollection> {

		@Override
		protected GameCollection doInBackground(Void... params) {
			GameCollection games = null;
			try {
				Game.List query = mService.game().list();
				query.setLimit(10L);
				games = query.execute();
			} catch (IOException e) {
				Log.e(CONSTANTS.TAG, "Welcome Query failed", e);
			}
			return games;
		}

		@Override
		protected void onPostExecute(GameCollection result) {
			super.onPostExecute(result);
			List<com.appspot.diamonds_app.diamonds.model.Game> games = null;
			
			if (result == null || result.getItems() == null) {
				Log.d(CONSTANTS.TAG, "Welcome Query failed is null");
				return;
				// games = new ArrayList<com.appspot.diamonds_app.diamonds.model.Game>();
			}else{
				games = result.getItems();
			}

			adapter = new GameArrayAdapter(WelcomeActivity.this,
					android.R.layout.simple_list_item_2, android.R.id.text1,
					games);

			mListView.setAdapter(adapter);

			Log.d(CONSTANTS.TAG, "Refreshed Games list");
		}

	}

}
