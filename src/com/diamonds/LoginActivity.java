package com.diamonds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnClickListener {

	private EditText mUsernameEditText;
	public static final String KEY_USERNAME = "Username";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mUsernameEditText = ((EditText) findViewById(R.id.username_edittext));
		((Button) findViewById(R.id.username_button)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		String username = mUsernameEditText.getText().toString();
		if (username.length() == 0) {
			return;
		}
		Intent game = new Intent(LoginActivity.this, WelcomeActivity.class);
		game.putExtra(KEY_USERNAME, username);
		startActivity(game);
	}
}
