package com.diamonds.app;

import java.io.IOException;
import java.util.Stack;
import java.util.TreeMap;

import org.rosehulman.edu.carterj3.Player;
import org.rosehulman.edu.carterj3.PlayerNotFoundException;

import com.appspot.diamonds_app.diamonds.Diamonds;
import com.appspot.diamonds_app.diamonds.model.Game;
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
import android.widget.TextView;
import android.widget.Button;

public class LobbyActivity extends Activity implements OnCommunication,
		OnClickListener {

	private String mIp;
	private String mUsername;
	private boolean mIsHost;
	private TextView chatOutput, chatInput;

	private TreeMap<Integer, Player> socketMap = new TreeMap<Integer, Player>();
	private Stack<Player> availableSlots = new Stack<Player>();
	private NonHostSocket sock;
	private boolean mIsPublic;
	private Diamonds mService;
	private Game currentGame;

	public static final String KEY_ISHOST = "ISHOST";
	public static final String KEY_IP = "IP";
	public static final String KEY_USERNAME = "USERNAME";

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
	protected void onDestroy() {
		super.onDestroy();

		Log.d(CONSTANTS.TAG, "OnDestroy L " + mUsername);

		for (Player p : socketMap.values()) {
			p.socket.closeSocket();
		}

		if (sock != null) {
			sock.closeSocket();
		}

		if (mIsHost && mIsPublic && (currentGame != null)) {
			(new DeleteGameTask()).execute(currentGame);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);

		Intent data = getIntent();
		mUsername = data.getStringExtra(LoginActivity.KEY_USERNAME);
		mIp = data.getStringExtra(WelcomeActivity.KEY_IP);
		mIsHost = data.getBooleanExtra(WelcomeActivity.KEY_ISHOST, false);
		mIsPublic = data.getBooleanExtra(WelcomeActivity.KEY_ISPUBLIC, false);

		chatOutput = ((TextView) findViewById(R.id.lobby_chat_output_textview));
		chatInput = ((TextView) findViewById(R.id.lobby_chat_input_textview));

		((Button) findViewById(R.id.lobby_chat_button))
				.setOnClickListener(this);

		Diamonds.Builder builder = new Diamonds.Builder(
				AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
		builder.setApplicationName("Diamonds-App");
		mService = builder.build();

		if (mIsHost) {
			Thread socketServerThread = new Thread(new SocketServerThread(this));
			socketServerThread.start();
			((TextView) findViewById(R.id.lobby_player1_textview))
					.setText(mUsername);

			// its a stack so add in reverse order
			availableSlots.add(new Player("Player 4", 3));
			availableSlots.add(new Player("Player 3", 2));
			availableSlots.add(new Player("Player 2", 1));

			if (mIsPublic) {
				Game newGame = new Game();
				newGame.setName(mUsername + "'s Game!");
				new InsertGameTask().execute(newGame);
			}

			((Button) findViewById(R.id.lobby_player2_action_button))
					.setOnClickListener(this);
			((Button) findViewById(R.id.lobby_player3_action_button))
					.setOnClickListener(this);
			((Button) findViewById(R.id.lobby_player4_action_button))
					.setOnClickListener(this);

		} else {
			sock = new NonHostSocket(this, mIp, mUsername);
			sock.start();

			((Button) findViewById(R.id.lobby_player2_action_button))
					.setVisibility(View.INVISIBLE);
			((Button) findViewById(R.id.lobby_player3_action_button))
					.setVisibility(View.INVISIBLE);
			((Button) findViewById(R.id.lobby_player4_action_button))
					.setVisibility(View.INVISIBLE);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void onRecv(final String msg, final int position) {
		Log.d(CONSTANTS.TAG, "Lobby [" + position + "] onRecv : " + msg);

		// Request for players
		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_GetUsernames)) {
			// If somebody asks for all of the usernames, send them

			String p1 = mUsername;
			String p2 = getPlayer(1);
			String p3 = getPlayer(2);
			String p4 = getPlayer(3);

			Log.d(CONSTANTS.TAG, CONSTANTS.SOCKET_SendUsernames + p1 + ";" + p2
					+ ";" + p3 + ";" + p4);

			Log.d(CONSTANTS.TAG, "GetUsernames id[" + position + "]");

			sendToPlayers(CONSTANTS.SOCKET_SendUsernames + p1 + ";" + p2 + ";"
					+ p3 + ";" + p4);

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendUsernames)) {
			String[] players = msg.split(":")[1].split(";");

			final String p1 = players[0];
			final String p2 = players[1];
			final String p3 = players[2];
			final String p4 = players[3];

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					((TextView) findViewById(R.id.lobby_player1_textview))
							.setText(p1);
					((TextView) findViewById(R.id.lobby_player2_textview))
							.setText(p2);
					((TextView) findViewById(R.id.lobby_player3_textview))
							.setText(p3);
					((TextView) findViewById(R.id.lobby_player4_textview))
							.setText(p4);
				}
			});

			Log.d(CONSTANTS.TAG, "recv_ " + msg);

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_GetUsername)) {
			// If somebody asks for our username, send it back
			if (!mIsHost) {
				return;
			}
			Log.d(CONSTANTS.TAG, "!!! [" + mIsHost + "] [" + mUsername + "]");
			socketMap.get(position).socket.send(CONSTANTS.SOCKET_SendUsername
					+ mUsername);
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendUsername)) {
			// If somebody send us their username, we should use it
			final String username = msg.split(":")[1];

			socketMap.get(position).name = username;

			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					switch (position) {
					case 0:
						((TextView) findViewById(R.id.lobby_player1_textview))
								.setText(username);
						break;
					case 1:
						((TextView) findViewById(R.id.lobby_player2_textview))
								.setText(username);
						break;
					case 2:
						((TextView) findViewById(R.id.lobby_player3_textview))
								.setText(username);
						break;
					case 3:
						((TextView) findViewById(R.id.lobby_player4_textview))
								.setText(username);
						break;
					}
				}
			});

			// Update everybody
			this.onRecv(CONSTANTS.SOCKET_GetUsernames, 0);

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendChat)) {
			// If somebody sends us a chat msg we should use display & forward
			// it

			if (msg.split(":")[1].equals("")) {
				return;
			}

			String name = getPlayer(position);
			final String message = name + " " + msg.split(":")[1];

			sendToPlayers(CONSTANTS.SOCKET_SendChat + message);

			addChatMessage("\n" + message);

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_StartGame)) {

			Log.d(CONSTANTS.TAG, "LobbyActivity starting game : " + position);

			if (mIsHost) {
				SocketServerThread.globalMap = this.socketMap;

			}
			if (mIsHost && mIsPublic && (currentGame != null)) {
				(new DeleteGameTask()).execute(currentGame);
			}

			Intent game = new Intent(LobbyActivity.this, GameActivity.class);
			game.putExtra(KEY_IP, mIp);
			game.putExtra(KEY_ISHOST, mIsHost);
			game.putExtra(KEY_USERNAME, mUsername);
			startActivity(game);
		}

	}

	private void addChatMessage(final String message) {
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				chatOutput.setText(chatOutput.getText().toString() + message);

			}
		});
	}

	private String getPlayer(int i) {
		if (i == 0) {
			return mUsername;
		}

		Player p = socketMap.get(i);
		if (p == null) {
			return "Player " + i;
		} else {
			return p.name;
		}
	}

	public void sendToPlayers(String msg) {
		if (!mIsHost) {
			return;
		}

		Log.d(CONSTANTS.TAG, "Lobby sendToPlayers l:" + socketMap.size());
		for (Player p : socketMap.values()) {
			p.socket.send(msg);
		}
	}

	public void sendToHost(String msg) {
		if (mIsHost) {
			// sendToPlayers(msg);
			onRecv(msg, 0);
		} else {
			sock.send(msg);
		}
	}

	@Override
	public Player onConnection(SocketServerReplyThread newSocket, int id)
			throws PlayerNotFoundException {
		if (availableSlots.size() == 0) {
			throw new PlayerNotFoundException();
		}

		Player newPlayer = availableSlots.pop();
		newPlayer.socket = newSocket;
		newPlayer.socketID = id;

		socketMap.put(newPlayer.position, newPlayer);
		newSocket.send(CONSTANTS.SOCKET_GetUsername);

		if (availableSlots.size() == 0) {
			(new StartGameTask()).execute();
		}

		changeToKickPlayer(newPlayer.position);

		return newPlayer;

	}

	private void changeToKickPlayer(final int position) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				switch (position) {
				case 1:
					((Button) findViewById(R.id.lobby_player2_action_button))
							.setText(R.string.lobby_kick_player_text);
					return;
				case 2:
					((Button) findViewById(R.id.lobby_player3_action_button))
							.setText(R.string.lobby_kick_player_text);
					return;
				case 3:
					((Button) findViewById(R.id.lobby_player4_action_button))
							.setText(R.string.lobby_kick_player_text);
					return;
				}
			}
		});

	}

	@Override
	public void onClick(View v) {
		String text;
		switch (v.getId()) {
		case R.id.lobby_chat_button:
			String newChat = chatInput.getText().toString();
			chatInput.setText("");

			sendToHost(CONSTANTS.SOCKET_SendChat + newChat);
			return;
		case R.id.lobby_player2_action_button:
			text = ((Button) findViewById(R.id.lobby_player2_action_button))
					.getText().toString();
			if (text.equals(getString(R.string.lobby_kick_player_text))) {
				socketMap.get(1).socket.closeSocket();
			} else {
				(new Bot("Bot")).StartBot();
			}
			return;
		case R.id.lobby_player3_action_button:
			text = ((Button) findViewById(R.id.lobby_player3_action_button))
					.getText().toString();
			if (text.equals(getString(R.string.lobby_kick_player_text))) {
				socketMap.get(2).socket.closeSocket();
			} else {
				(new Bot("Bot")).StartBot();
			}
			return;
		case R.id.lobby_player4_action_button:
			text = ((Button) findViewById(R.id.lobby_player4_action_button))
					.getText().toString();
			if (text.equals(getString(R.string.lobby_kick_player_text))) {
				socketMap.get(3).socket.closeSocket();
			} else {
				(new Bot("Bot")).StartBot();
			}
			return;
		}

	}

	@Override
	public void onDisconnect(Player player) {

		if (!mIsHost) {
			// Go back to welcome screen
			finish();
			return;
		}

		final int position = player.position;
		boolean found = false;
		socketMap.remove(player.position);
		for (Player p : availableSlots) {
			if (p.position == position) {
				found = true;
			}
		}
		if (!found) {
			availableSlots.add(new Player("Player " + (player.position + 1),
					player.position));
		}

		onRecv(CONSTANTS.SOCKET_GetUsernames, 0);

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				switch (position) {
				case 1:
					((TextView) findViewById(R.id.lobby_player2_textview))
							.setText("Player 2");
					((Button) findViewById(R.id.lobby_player2_action_button))
							.setText(R.string.lobby_add_bot);
					return;
				case 2:
					((TextView) findViewById(R.id.lobby_player3_textview))
							.setText("Player 3");
					((Button) findViewById(R.id.lobby_player3_action_button))
							.setText(R.string.lobby_add_bot);
					return;
				case 3:
					((TextView) findViewById(R.id.lobby_player4_textview))
							.setText("Player 4");
					((Button) findViewById(R.id.lobby_player4_action_button))
							.setText(R.string.lobby_add_bot);
					return;
				}
			}
		});

		player = null;
	}

	class StartGameTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			for (int i = 5; i > 0; i--) {
				if (availableSlots.size() == 0) {
					return null;
				}
				sendToHost(CONSTANTS.SOCKET_SendChat
						+ String.format("Game starting in %d ...", i));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (availableSlots.size() == 0) {
				onRecv(CONSTANTS.SOCKET_StartGame, 0);
			}
		}

	}

	class InsertGameTask extends AsyncTask<Game, Void, Game> {

		@Override
		protected Game doInBackground(Game... params) {
			Game returnedGame = null;
			try {
				returnedGame = mService.game().insert(params[0]).execute();
			} catch (IOException e) {
				Log.e(CONSTANTS.TAG, "Lobby Insert failed.", e);
			}
			return returnedGame;
		}

		@Override
		protected void onPostExecute(Game result) {
			super.onPostExecute(result);

			currentGame = result;
		}

	}

	class DeleteGameTask extends AsyncTask<Game, Void, Game> {
		@Override
		protected Game doInBackground(Game... params) {
			Game returnedGame = null;
			try {
				returnedGame = mService.game().delete(params[0].getEntityKey())
						.execute();
			} catch (IOException e) {
				Log.e(CONSTANTS.TAG, "Lobby Delete failed.", e);
			}
			return returnedGame;
		}
	}
}
