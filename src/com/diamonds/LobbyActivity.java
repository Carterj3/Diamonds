package com.diamonds;

import java.util.ArrayList;
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
	
	public static final String KEY_ISHOST = "ISHOST";
	public static final String KEY_IP = "IP";
	public static final String KEY_USERNAME = "USERNAME";

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
			((TextView) findViewById(R.id.lobby_player1_textview)).setText(mUsername);
			
			// its a stack so add in reverse order
			availableSlots.add(new Player("Player 4",3));
			availableSlots.add(new Player("Player 3",2));
			availableSlots.add(new Player("Player 2",1));
		} else {
			sock = new NonHostSocket(this, mIp,mUsername);
			sock.start();

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onRecv(final String msg, final int position) {
		Log.d(MainActivity.tag, "Lobby onRecv : " + msg);

		// Request for players
		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_GetUsernames)) {
			// If somebody asks for all of the usernames, send them
			
			
			
			String p1 = mUsername;
			String p2 = getPlayer(1);
			String p3 = getPlayer(2);
			String p4 = getPlayer(3);

			Log.d(MainActivity.tag, CONSTANTS.SOCKET_SendUsernames + p1 + ";"
					+ p2 + ";" + p3 + ";" + p4);

			Log.d(MainActivity.tag,"GetUsernames id["+position+"]");

			sendToPlayers(CONSTANTS.SOCKET_SendUsernames + p1 + ";" + p2 + ";" + p3
					+ ";" + p4);
			
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
			socketMap.get(position).socket.send(CONSTANTS.SOCKET_SendUsername + mUsername);
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendUsername)) {
			// If somebody send us their username, we should use it
			final String username = msg.split(":")[1];
			
			socketMap.get(position).name = username;
			
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					switch (position) {
					case 0:
						((TextView) findViewById(R.id.lobby_player1_textview)).setText(username);
						break;
					case 1:
						((TextView) findViewById(R.id.lobby_player2_textview)).setText(username);
						break;
					case 2:
						((TextView) findViewById(R.id.lobby_player3_textview)).setText(username);
						break;
					case 3:
						((TextView) findViewById(R.id.lobby_player4_textview)).setText(username);
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
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					chatOutput.setText(chatOutput.getText().toString() + "\n"
							+ msg.split(":")[1]);

				}
			});
			
			if(msg.split(":")[1].equals("Start")){
				Log.d(MainActivity.tag,"LobbyActivity going to start game : "+position);
				onRecv(CONSTANTS.SOCKET_StartGame, 0);
			}

		} else if(CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_StartGame)){
			
			Log.d(MainActivity.tag,"LobbyActivity starting game : "+position);
			
			if(mIsHost){
				SocketServerThread.globalMap = this.socketMap;
				sendToPlayers(msg);
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
		if(p==null){
			return "Player "+i;
		}else{
			return p.name;
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
	public Player onConnection(SocketServerReplyThread newSocket, int id) throws PlayerNotFoundException {
		if(availableSlots.size() == 0){
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
		
		if(!mIsHost){
			// Go back to welcome screen
			finish();
			return;
		}
		
		socketMap.remove(player.position);
		availableSlots.add(new Player("Player "+(player.position+1), player.position));
		player = null;
	}

}
