package com.diamonds;

import java.util.TreeMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

	private TreeMap<Integer, SocketServerReplyThread> socketMap = new TreeMap<Integer, SocketServerReplyThread>();
	private NonHostSocket sock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);

		Intent data = getIntent();
		mUsername = data.getStringExtra(LoginActivity.KEY_USERNAME);
		mIp = data.getStringExtra(WelcomeActivity.KEY_IP);
		mIsHost = data.getBooleanExtra(WelcomeActivity.KEY_ISHOST, false);

		chatOutput = ((TextView) findViewById(R.id.lobby_chat_output_textview));
		chatInput = ((TextView) findViewById(R.id.lobby_chat_input_textview));

		((Button) findViewById(R.id.lobby_chat_button))
				.setOnClickListener(this);

		if (mIsHost) {
			Thread socketServerThread = new Thread(new SocketServerThread(this));
			socketServerThread.start();
			((TextView) findViewById(R.id.lobby_player1_textview))
					.setText(mUsername);
		} else {
			sock = new NonHostSocket(this, mIp);
			sock.start();

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onRecv(final String msg, final int id) {
		Log.d(MainActivity.tag, "Lobby onRecv : " + msg);

		// Request for players
		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_GetUsernames)) {
			// If somebody asks for all of the usernames, send them
			String p1 = ((TextView) findViewById(R.id.lobby_player1_textview))
					.getText().toString();
			String p2 = ((TextView) findViewById(R.id.lobby_player2_textview))
					.getText().toString();
			String p3 = ((TextView) findViewById(R.id.lobby_player3_textview))
					.getText().toString();
			String p4 = ((TextView) findViewById(R.id.lobby_player4_textview))
					.getText().toString();

			Log.d(MainActivity.tag, CONSTANTS.SOCKET_SendUsernames + p1 + ";"
					+ p2 + ";" + p3 + ";" + p4);

			socketMap.get(id).send(
					CONSTANTS.SOCKET_SendUsernames + p1 + ";" + p2 + ";" + p3
							+ ";" + p4);
			Log.d(MainActivity.tag,"GetUsernames id["+id+"] socketmap["+socketMap.get(id).toString()+"]");

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendUsernames)) {
			String[] players = msg.split(":")[1].split(";");

			final String p1 = players[0];
			final String p2 = players[1];
			final String p3 = players[2];
			final String p4 = players[3];

		
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					((TextView) findViewById(R.id.lobby_player1_textview)).setText(p1);
					((TextView) findViewById(R.id.lobby_player2_textview)).setText(p2);
					((TextView) findViewById(R.id.lobby_player3_textview)).setText(p3);
					((TextView) findViewById(R.id.lobby_player4_textview)).setText(p4);
				}
			});
			
			Log.d(MainActivity.tag, "recv_ "+msg);
			
			

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_GetUsername)) {
			// If somebody asks for our username, send it back
			socketMap.get(id).send(CONSTANTS.SOCKET_SendUsername + mUsername);
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendUsername)) {
			// If somebody send us their username, we should use it
			final String username = msg.split(":")[1];
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					switch (id + 1) {
					case 1:
						((TextView) findViewById(R.id.lobby_player1_textview))
								.setText(username);
						break;
					case 2:
						((TextView) findViewById(R.id.lobby_player2_textview))
								.setText(username);
						break;
					case 3:
						((TextView) findViewById(R.id.lobby_player3_textview))
								.setText(username);
						break;
					case 4:
						((TextView) findViewById(R.id.lobby_player4_textview))
								.setText(username);
						break;
					}
				}
			});

			// let other players know that somebody joined
			sendToPlayers(msg);
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendChat)) {
			// If somebody sends us a chat msg we should use display & forward
			// it
			sendToPlayers(msg);
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					chatOutput.setText(chatOutput.getText().toString() + "\n"
							+ msg.split(":")[1]);

				}
			});

		}

	}

	public void sendToPlayers(String msg) {
		if (!mIsHost) {
			return;
		}

		Log.d(MainActivity.tag, "Lobby sendToPlayers l:" + socketMap.size());
		for (SocketServerReplyThread sock : socketMap.values()) {
			sock.send(msg);
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
	public void onConnection(SocketServerReplyThread newSocket, int id) {
		socketMap.put(id, newSocket);
		newSocket.send(CONSTANTS.SOCKET_GetUsername);

	}

	@Override
	public void onClick(View v) {
		String newChat = chatInput.getText().toString();
		chatInput.setText("");

		sendToHost(CONSTANTS.SOCKET_SendChat + newChat);
	}

}
