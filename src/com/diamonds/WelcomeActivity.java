package com.diamonds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		Intent data = getIntent();
		mUsername = data.getStringExtra(LoginActivity.KEY_USERNAME);

		mIp = ((EditText) findViewById(R.id.join_game_edittext));
		((Button) findViewById(R.id.join_game_button)).setOnClickListener(this);
		((Button) findViewById(R.id.create_game_button)).setOnClickListener(this);
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
