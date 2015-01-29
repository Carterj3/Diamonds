package com.diamonds;

import java.util.Stack;
import java.util.TreeMap;

import org.rosehulman.edu.carterj3.Player;
import org.rosehulman.edu.carterj3.PlayerNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;;

public class GameActivity extends Activity implements OnCommunication,
		OnClickListener {

	private String mIp;
	private String mUsername;
	private boolean mIsHost;

	private TreeMap<Integer, Player> socketMap = new TreeMap<Integer, Player>();
	private NonHostSocket sock;

	protected TextView chatOutput;
	protected EditText chatInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		Intent data = getIntent();

		mIp = data.getStringExtra(LobbyActivity.KEY_IP);
		mIsHost = data.getBooleanExtra(LobbyActivity.KEY_ISHOST, false);
		mUsername = data.getStringExtra(LobbyActivity.KEY_USERNAME);

		chatOutput = ((TextView) findViewById(R.id.game_chat_textview));
		chatInput = ((EditText) findViewById(R.id.game_chat_edittext));

		((Button) findViewById(R.id.game_chat_button)).setOnClickListener(this);

		if (mIsHost) {
			SocketServerThread.globalSocket.comListener = this;
			for (Player p : SocketServerThread.globalMap.values()) {
				p.socket.comListener = this;
			}
			socketMap = SocketServerThread.globalMap;
		} else {
			NonHostSocket.globalSocket.comListener = this;
			sock = NonHostSocket.globalSocket;
		}

	}

	@Override
	public void onRecv(final String msg, final int id) {
		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendChat)) {
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
		// TODO Auto-generated method stub
		throw new PlayerNotFoundException();
	}

	@Override
	public void onDisconnect(Player player) {
		if (!mIsHost) {
			return;
		}
		socketMap.remove(player);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.game_chat_button:
			String newChat = chatInput.getText().toString();
			chatInput.setText("");

			sendToHost(CONSTANTS.SOCKET_SendChat + newChat);
			break;
		default:
			break;
		}
	}
}
