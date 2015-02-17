package com.diamonds;

import java.util.ArrayList;
import java.util.TreeMap;

import org.rosehulman.edu.carterj3.BidAction;
import org.rosehulman.edu.carterj3.Card;
import org.rosehulman.edu.carterj3.DealCardsAction;
import org.rosehulman.edu.carterj3.GameEngine;
import org.rosehulman.edu.carterj3.GameEngine.GameState;
import org.rosehulman.edu.carterj3.InitGameAction;
import org.rosehulman.edu.carterj3.PlayCardAction;
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
import android.widget.Toast;

public class GameActivity extends Activity implements OnCommunication,
		OnClickListener {

	private String mIp;
	private String mUsername;
	private boolean mIsHost;

	private boolean isBid = true;

	private TreeMap<Integer, Player> socketMap = new TreeMap<Integer, Player>();

	private NonHostSocket sock;

	protected TextView chatOutput;
	protected EditText chatInput;

	GameEngine engine;

	ArrayList<Card> mHand;
	Card leadCard;

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.d(MainActivity.tag, "OnDestroy G " + mUsername);

		for (Player p : socketMap.values()) {
			if (p.socket != null) {
				p.socket.closeSocket();
			}
		}

		if (sock != null) {
			sock.closeSocket();
		}
	}

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
		((Button) findViewById(R.id.game_table_bid_button))
				.setOnClickListener(this);

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
		onRecv(CONSTANTS.SOCKET_SendHand + convertHandToString(hand), 0);
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

		if (s.equals("")) {
			return s;
		}
		s = s.substring(1);
		String msg = s;
		return msg;
	}

	public static ArrayList<Card> convertStringToHand(String str) {
		ArrayList<Card> hand = new ArrayList<Card>(13);

		if (str.equals("")) {
			return hand;
		}

		try {
			for (String s : str.split(" ")) {
				hand.add(new Card(s));
			}
		} catch (Exception e) {
			Log.e(MainActivity.tag, "convertStringToHand " + str, e);
		}
		return hand;
	}

	private void sendHand(ArrayList<Card> hand, Integer player) {
		String msg = CONSTANTS.SOCKET_SendHand + convertHandToString(hand);
		sendToSocket(socketMap.get(player), msg);
	}

	@Override
	public void onRecv(final String msg, final int id) {
		Log.d(MainActivity.tag, "Game [" + id + "] onRecv : " + msg);

		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_YourTurn)) {
			isBid = false;

			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(GameActivity.this, "Your turn!",
							Toast.LENGTH_SHORT).show();
				}

			});
		}

		else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_ScoreSummary)) {
			final String[] splits = msg.split(":")[1].split(" ");
			isBid = true;

			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((TextView) findViewById(R.id.game_player1_score_textview))
							.setText(splits[0]);
					((TextView) findViewById(R.id.game_player2_score_textview))
							.setText(splits[1]);
					((TextView) findViewById(R.id.game_player3_score_textview))
							.setText(splits[2]);
					((TextView) findViewById(R.id.game_player4_score_textview))
							.setText(splits[3]);

				}
			});
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_TrickSummary)) {
			final String[] splits = msg.split(":")[1].split(" ");

			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((TextView) findViewById(R.id.game_table_player1_card_textview))
							.setText("");
					((TextView) findViewById(R.id.game_table_player2_card_textview))
							.setText("");
					((TextView) findViewById(R.id.game_table_player3_card_textview))
							.setText("");
					((TextView) findViewById(R.id.game_table_player4_card_textview))
							.setText("");

					((TextView) findViewById(R.id.game_player1_points_textview))
							.setText(splits[0]);
					((TextView) findViewById(R.id.game_player2_points_textview))
							.setText(splits[1]);
					((TextView) findViewById(R.id.game_player3_points_textview))
							.setText(splits[2]);
					((TextView) findViewById(R.id.game_player4_points_textview))
							.setText(splits[3]);

				}
			});
		}

		else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_PlayedCard)) {
			final Integer player = Integer.parseInt(msg.split(":")[1]);
			final String card = msg.split(":")[2];

			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					switch (player) {
					case 0:
						((TextView) findViewById(R.id.game_table_player1_card_textview))
								.setText(card);
						break;
					case 1:
						((TextView) findViewById(R.id.game_table_player2_card_textview))
								.setText(card);
						break;
					case 2:
						((TextView) findViewById(R.id.game_table_player3_card_textview))
								.setText(card);
						break;
					case 3:
						((TextView) findViewById(R.id.game_table_player4_card_textview))
								.setText(card);
						break;
					}
				}
			});
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_PlayCard)) {
			Player p = socketMap.get(id);
			String card_str = msg.split(":")[1];
			PlayCardAction cardAction = new PlayCardAction(new Card(card_str),
					p);

			if (!engine.HandleAction(cardAction)) {
				try {
					Player lead = socketMap.get(engine.order.get(0).position);
					sendToSocket(p, CONSTANTS.SOCKET_SendHand
							+ convertHandToString(engine.getPlayer(p).hand));
					sendToSocket(lead, CONSTANTS.SOCKET_YourTurn);

				} catch (PlayerNotFoundException e) {
					Log.e(MainActivity.tag, "SOCKET_PlayCard PNFE", e);
				}
				return;
			}

			try {
				sendToSocket(p, CONSTANTS.SOCKET_SendHand
						+ convertHandToString(engine.getPlayer(p).hand));
			} catch (PlayerNotFoundException e) {
				Log.e(MainActivity.tag, "SOCKET_PlayCard PNFE", e);
				return;
			}

			sendToPlayers(CONSTANTS.SOCKET_PlayedCard + id + ":" + card_str);
			onRecv(CONSTANTS.SOCKET_PlayedCard + id + ":" + card_str, 0);

			switch (engine.getState()) {
			case ROUND_END:
				Log.d(MainActivity.tag, "Round over");
				onRecv(CONSTANTS.SOCKET_SendChat + "Round over", 0);

				StartGameAction newRound = new StartGameAction();
				engine.HandleAction(newRound);

				updateScore();
				updateTricks();

				switch (engine.getState()) {
				case GAME_OVER:
					Log.d(MainActivity.tag, "Game over");
					onRecv(CONSTANTS.SOCKET_SendChat + "Game over", 0);
					break;
				default:
					engine.HandleAction(newRound);
					dealCards();
				}

				break;
			case TRICK_START:
				updateTricks();
			default:
				Player lead = socketMap.get(engine.order.get(0).position);
				sendToSocket(lead, CONSTANTS.SOCKET_YourTurn);
			}

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendBid)) {
			if (!(engine.getState() == GameState.WAITING_FOR_BIDS)) {
				return;
			}

			Player p = socketMap.get(id);
			Integer bid = Integer.parseInt(msg.split(":")[1]);

			BidAction bidAction = new BidAction(p, bid);
			engine.HandleAction(bidAction);

			if (engine.getState() == GameState.ROUND_START) {
				Log.d(MainActivity.tag, "Everybody bid");
				onRecv(CONSTANTS.SOCKET_SendChat + "Everybody bid", 0);

				Player lead = socketMap.get(engine.order.get(0).position);
				sendToSocket(lead, CONSTANTS.SOCKET_YourTurn);

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
			String hand_str_t = "";
			if (msg.split(":").length > 1) {
				hand_str_t = msg.split(":")[1];
				Log.d(MainActivity.tag, "Hand length [" + mUsername + "] "
						+ hand_str_t.split(" ").length);
			}

			final String hand_str = hand_str_t;

			mHand = convertStringToHand(hand_str);

			// -- display hand
			// ???
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					((Button) findViewById(R.id.game_table_bid_button))
							.setVisibility(View.VISIBLE);
					((EditText) findViewById(R.id.game_table_bid_edittext))
							.setVisibility(View.VISIBLE);

					((TextView) findViewById(R.id.game_table_player_hand_textview))
							.setText(hand_str);
				}
			});

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendChat)) {
			// If somebody sends us a chat msg we should use display & forward
			// it
			sendToPlayers(msg);
			String message = "\n" + msg.split(":")[1];
			addChatMessage(message);

		}

	}

	private void updateScore() {
		String s = "";
		for (int j = 0; j <= 3; j++) {
			for (Player player : engine.order) {
				if (player.position == j) {
					s = s + " " + player.score;
				}
			}

		}
		s = s.substring(1);

		sendToPlayers(CONSTANTS.SOCKET_ScoreSummary + s);
		onRecv(CONSTANTS.SOCKET_ScoreSummary + s, 0);
	}

	private void updateTricks() {
		String s = "";

		for (int j = 0; j <= 3; j++) {
			for (Player player : engine.order) {
				if (player.position == j) {
					s = s + " " + player.points;
				}
			}

		}
		s = s.substring(1);

		sendToPlayers(CONSTANTS.SOCKET_TrickSummary + s);
		onRecv(CONSTANTS.SOCKET_TrickSummary + s, 0);
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
			if (p.position != 0) {
				p.socket.send(msg);
			}
		}
	}

	public void sendToSocket(Player p, String msg) {
		if (p.position == 0) {
			onRecv(msg, 0);
		} else {
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
		Log.d(MainActivity.tag, "Player [" + player.name
				+ "] disconnected from [" + player.position + "]");
		socketMap.remove(player.position);
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
			String bid = ((EditText) findViewById(R.id.game_table_bid_edittext))
					.getText().toString();
			if (isBid) {

				sendToHost(CONSTANTS.SOCKET_SendBid + bid);
			} else {
				try {
					sendToHost(CONSTANTS.SOCKET_PlayCard
							+ mHand.get(Integer.parseInt(bid)));
				} catch (NumberFormatException e1) {

				}

				catch (IndexOutOfBoundsException e2) {

				}
			}

			break;
		default:
			break;
		}
	}
}
