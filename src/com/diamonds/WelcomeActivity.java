package com.diamonds;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class WelcomeActivity extends Activity implements OnClickListener {

	private EditText mIp;
	private String mUsername;
	public static final String KEY_IP = "IP";
	public static final String KEY_ISHOST = "ISHOST";

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
		((Button) findViewById(R.id.join_game_button)).setOnClickListener(this);
		((Button) findViewById(R.id.create_game_button))
				.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		String ip = mIp.getText().toString();
		if (v.getId() == R.id.create_game_button) {
			Intent lobby = new Intent(WelcomeActivity.this, LobbyActivity.class);
			lobby.putExtra(KEY_IP, ip);
			lobby.putExtra(LoginActivity.KEY_USERNAME, mUsername);
			lobby.putExtra(KEY_ISHOST, true);
			startActivity(lobby);
		}

		if (ip.length() > 0 && v.getId() == R.id.join_game_button) {
			Intent lobby = new Intent(WelcomeActivity.this, LobbyActivity.class);
			lobby.putExtra(KEY_IP, ip);
			lobby.putExtra(LoginActivity.KEY_USERNAME, mUsername);
			lobby.putExtra(KEY_ISHOST, false);
			startActivity(lobby);
		}

	}
}
