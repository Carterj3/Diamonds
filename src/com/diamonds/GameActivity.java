package com.diamonds;

import java.util.ArrayList;
import java.util.TreeMap;

import org.rosehulman.edu.carterj3.BidAction;
import org.rosehulman.edu.carterj3.Card;
import org.rosehulman.edu.carterj3.DealCardsAction;
import org.rosehulman.edu.carterj3.GameEngine;
import org.rosehulman.edu.carterj3.GameEngine.GameState;
import org.rosehulman.edu.carterj3.InitGameAction;
import org.rosehulman.edu.carterj3.Player;
import org.rosehulman.edu.carterj3.PlayerNotFoundException;
import org.rosehulman.edu.carterj3.StartGameAction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;

;

public class GameActivity extends Activity implements OnCommunication,
		OnClickListener {

	private String mIp;
	private String mUsername;
	private boolean mIsHost;

	private TreeMap<Integer, Player> socketMap = new TreeMap<Integer, Player>();

	private NonHostSocket sock;

	protected TextView chatOutput;
	protected EditText chatInput;

	GameEngine engine;

	ArrayList<Card> mHand;
	Card leadCard;

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
		//((Button) findViewById(R.id.game_table_bid_button)).setOnClickListener(this);
		
		// set sockets
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

		// start the game
		if (mIsHost) {
			engine = new GameEngine();

			socketMap.put(0, new Player(mUsername, 0));

			Player p1 = socketMap.get(0);

			InitGameAction initGame = new InitGameAction(p1, 0);
			engine.HandleAction(initGame);

			sendToPlayers(CONSTANTS.SOCKET_StartGame);
		} else {
			sock.send(CONSTANTS.SOCKET_IsReady);
		}

	}

	private void dealCards() {
		DealCardsAction dealCards = new DealCardsAction();
		engine.HandleAction(dealCards);

		// Add our hand to ourselfs
		ArrayList<Card> hand = engine.player1.hand;
		onRecv(convertHandToString(hand), 0);
		// For each player send them their hand
		hand = engine.player2.hand;
		sendHand(hand, 1);

		hand = engine.player3.hand;
		sendHand(hand, 2);

		hand = engine.player4.hand;
		sendHand(hand, 3);

	}

	public static String convertHandToString(ArrayList<Card> hand) {
		String s = "";
		for (Card c : hand) {
			s = s + " " + c.toString();
		}
		s = s.substring(1);
		String msg = CONSTANTS.SOCKET_SendHand + s;
		return msg;
	}

	public static ArrayList<Card> convertStringToHand(String str) {
		ArrayList<Card> hand = new ArrayList<Card>(13);
		for (String s : str.split(" ")) {
			hand.add(new Card(s));
		}
		return hand;
	}

	private void sendHand(ArrayList<Card> hand, Integer player) {
		String msg = convertHandToString(hand);
		socketMap.get(player).socket.send(msg);
	}

	@Override
	public void onRecv(final String msg, final int id) {
		Log.d(MainActivity.tag, "Game [" + id + "] onRecv : " + msg);

		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendBid)) {
			Player p = socketMap.get(id);
			Integer bid = Integer.parseInt(msg.split(":")[1]);

			BidAction bidAction = new BidAction(p, bid);
			engine.HandleAction(bidAction);

			if (engine.getState() == GameState.ROUND_START) {
				Log.d(MainActivity.tag, "Everybody bid");
				onRecv(CONSTANTS.SOCKET_SendChat + "Everybody bid", 0);
			}
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_IsReady)) {
			Player p = socketMap.get(id);

			InitGameAction init = new InitGameAction(p, id);
			engine.HandleAction(init);

			if (engine.getState() == GameState.INITIALIZED) {
				StartGameAction startGame = new StartGameAction();
				engine.HandleAction(startGame);

				dealCards();
			}

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendHand)) {
			String hand_str = msg.split(":")[1];
			mHand = convertStringToHand(hand_str);
			addChatMessage(id + "::" + hand_str);

			((Button) findViewById(R.id.game_table_bid_button))
					.setVisibility(View.VISIBLE);
			((EditText) findViewById(R.id.game_table_bid_edittext))
					.setVisibility(View.VISIBLE);

			// -- display hand
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendChat)) {
			// If somebody sends us a chat msg we should use display & forward
			// it
			sendToPlayers(msg);
			String message = "\n" + msg.split(":")[1];
			addChatMessage(message);

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

			sendToHost(CONSTANTS.SOCKET_SendChat
					+ (newChat.equals("") ? " " : newChat));
			break;
		case R.id.game_table_bid_button:
			String bid = ((EditText)findViewById(R.id.game_table_bid_edittext)).getText().toString();
			sendToHost(CONSTANTS.SOCKET_SendBid+bid);
			break;
		default:
			break;
		}
	}
}
