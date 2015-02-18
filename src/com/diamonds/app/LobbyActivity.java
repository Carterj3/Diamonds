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

			Bot.bot1 = new Bot("Bot 1", 1);
			Bot.bot1.StartBot();

			Bot.bot2 = new Bot("Bot 2", 2);
			Bot.bot2.StartBot();

			if (mIsPublic) {
				Game newGame = new Game();
				newGame.setName(mUsername + "'s Game!");
				new InsertGameTask().execute(newGame);
			}

		} else {
			sock = new NonHostSocket(this, mIp, mUsername);
			sock.start();

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
			sendToPlayers(msg);

			if (msg.split(":")[1].equals("")) {
				return;
			}

			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					chatOutput.setText(chatOutput.getText().toString() + "\n"
							+ msg.split(":")[1]);

				}
			});

			if (msg.split(":")[1].equals("Start") && mIsHost) {
				Log.d(CONSTANTS.TAG, "LobbyActivity going to start game : "
						+ position);
				onRecv(CONSTANTS.SOCKET_StartGame, 0);
			}

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

	private String getPlayer(int i) {
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

		return newPlayer;

	}

	@Override
	public void onClick(View v) {
		String newChat = chatInput.getText().toString();
		chatInput.setText("");

		sendToHost(CONSTANTS.SOCKET_SendChat + newChat);
	}

	@Override
	public void onDisconnect(Player player) {

		if (!mIsHost) {
			// Go back to welcome screen
			finish();
			return;
		}

		socketMap.remove(player.position);
		availableSlots.add(new Player("Player " + (player.position + 1),
				player.position));
		player = null;
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
