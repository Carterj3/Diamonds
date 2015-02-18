package com.diamonds.app;

import java.util.ArrayList;
import java.util.Collections;
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

import com.diamonds.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

public class GameActivity extends Activity implements OnCommunication,
		OnClickListener {

	private static final int SleepBetweenCards = 800;
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

		Log.d(CONSTANTS.TAG, "OnDestroy G " + mUsername);

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

		String p1 = data.getStringExtra(WelcomeActivity.KEY_Player1);
		String p2 = data.getStringExtra(WelcomeActivity.KEY_Player2);
		String p3 = data.getStringExtra(WelcomeActivity.KEY_Player3);
		String p4 = data.getStringExtra(WelcomeActivity.KEY_Player4);

		((TextView) findViewById(R.id.game_table_player1_name_textview))
				.setText(p1);
		((TextView) findViewById(R.id.game_table_player2_name_textview))
				.setText(p2);
		((TextView) findViewById(R.id.game_table_player3_name_textview))
				.setText(p3);
		((TextView) findViewById(R.id.game_table_player4_name_textview))
				.setText(p4);

		((TextView) findViewById(R.id.game_player1_name_textview)).setText(p1);
		((TextView) findViewById(R.id.game_player2_name_textview)).setText(p2);
		((TextView) findViewById(R.id.game_player3_name_textview)).setText(p3);
		((TextView) findViewById(R.id.game_player4_name_textview)).setText(p4);

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

			Player player1 = socketMap.get(0);

			InitGameAction initGame = new InitGameAction(player1, 0);
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
			Log.e(CONSTANTS.TAG, "convertStringToHand " + str, e);
		}

		return sortHand(hand);
	}

	private static ArrayList<Card> sortHand(ArrayList<Card> hand) {
		ArrayList<Card> hearts = new ArrayList<Card>();
		ArrayList<Card> diamonds = new ArrayList<Card>();
		ArrayList<Card> clubs = new ArrayList<Card>();
		ArrayList<Card> spades = new ArrayList<Card>();

		for (Card c : hand) {
			switch (c.suit) {
			case Heart:
				hearts.add(c);
				break;
			case Diamond:
				diamonds.add(c);
				break;
			case Club:
				clubs.add(c);
				break;
			case Spade:
				spades.add(c);
				break;
			}
		}

		Collections.sort(hearts);
		Collections.sort(diamonds);
		Collections.sort(clubs);
		Collections.sort(spades);

		clubs.addAll(hearts);
		clubs.addAll(spades);
		clubs.addAll(diamonds);

		return clubs;

	}

	private void sendHand(ArrayList<Card> hand, Integer player) {
		String msg = CONSTANTS.SOCKET_SendHand + convertHandToString(hand);
		sendToSocket(socketMap.get(player), msg);
	}

	@Override
	public void onRecv(final String msg, final int id) {
		Log.d(CONSTANTS.TAG, "Game [" + id + "] onRecv : " + msg);

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

					TextView bidView = (TextView) findViewById(R.id.game_bid_textview);
					bidView.setText(R.string.game_no_bid_text);

					((EditText) findViewById(R.id.game_table_bid_edittext))
							.setVisibility(View.VISIBLE);
					((Button) findViewById(R.id.game_table_bid_button))
							.setVisibility(View.VISIBLE);

				}
			});
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_TrickSummary)) {
			final String[] splits = msg.split(":")[1].split(" ");

			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((ImageView) findViewById(R.id.game_table_player1_card_imageview))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.card_back));
					((ImageView) findViewById(R.id.game_table_player2_card_imageview))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.card_back));
					((ImageView) findViewById(R.id.game_table_player3_card_imageview))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.card_back));
					((ImageView) findViewById(R.id.game_table_player4_card_imageview))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.card_back));

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

					((EditText) findViewById(R.id.game_table_bid_edittext))
							.setVisibility(View.INVISIBLE);
					((Button) findViewById(R.id.game_table_bid_button))
							.setVisibility(View.INVISIBLE);

					Card c = new Card(card);
					Drawable d = getResources().getDrawable(
							getResources().getIdentifier(c.getResourceName(),
									"drawable", getPackageName()));

					switch (player) {
					case 0:
						((ImageView) findViewById(R.id.game_table_player1_card_imageview))
								.setImageDrawable(d);
						break;
					case 1:
						((ImageView) findViewById(R.id.game_table_player2_card_imageview))
								.setImageDrawable(d);

						break;
					case 2:
						((ImageView) findViewById(R.id.game_table_player3_card_imageview))
								.setImageDrawable(d);

						break;
					case 3:
						((ImageView) findViewById(R.id.game_table_player4_card_imageview))
								.setImageDrawable(d);

						break;
					}
				}
			});
			try {
				Thread.sleep(SleepBetweenCards);
			} catch (InterruptedException e) {
			}
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
					Log.e(CONSTANTS.TAG, "SOCKET_PlayCard PNFE", e);
				}
				return;
			}

			try {
				sendToSocket(p, CONSTANTS.SOCKET_SendHand
						+ convertHandToString(engine.getPlayer(p).hand));
			} catch (PlayerNotFoundException e) {
				Log.e(CONSTANTS.TAG, "SOCKET_PlayCard PNFE", e);
				return;
			}

			sendToPlayers(CONSTANTS.SOCKET_PlayedCard + id + ":" + card_str);
			onRecv(CONSTANTS.SOCKET_PlayedCard + id + ":" + card_str, 0);

			switch (engine.getState()) {
			case ROUND_END:
				Log.d(CONSTANTS.TAG, "Round over");
				onRecv(CONSTANTS.SOCKET_SendChat + "Round over", 0);

				StartGameAction newRound = new StartGameAction();
				engine.HandleAction(newRound);

				updateScore();
				updateTricks();

				switch (engine.getState()) {
				case GAME_OVER:
					Log.d(CONSTANTS.TAG, "Game over");
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

			if (msg.split(":").length < 2) {
				return;
			}

			Player p = socketMap.get(id);
			Integer bid = Integer.parseInt(msg.split(":")[1]);

			BidAction bidAction = new BidAction(p, bid);
			engine.HandleAction(bidAction);

			if (engine.getState() == GameState.ROUND_START) {
				Log.d(CONSTANTS.TAG, "Everybody bid");
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
				Log.d(CONSTANTS.TAG, "Hand length [" + mUsername + "] "
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

					LinearLayout ll = (LinearLayout) findViewById(R.id.game_table_player_hand_layout);
					ll.removeAllViews();

					for (final Card c : mHand) {
						ImageView iv = new ImageView(GameActivity.this);
						iv.setImageDrawable(getResources().getDrawable(
								getResources().getIdentifier(
										c.getResourceName(), "drawable",
										getPackageName())));
						iv.setLayoutParams(new LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
						iv.setPadding(5, 5, 5, 5);

						iv.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								if (!isBid) {
									sendToHost(CONSTANTS.SOCKET_PlayCard + c);
								}

							}
						});

						ll.addView(iv);
					}
				}
			});

		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendChat)) {
			// If somebody sends us a chat msg we should use display & forward
			// it
			

			if (msg.split(":")[1].equals("")) {
				return;
			}

			String name = socketMap.get(id).name;
			String message = name + " " + msg.split(":")[1];

			sendToPlayers(CONSTANTS.SOCKET_SendChat + message);
			addChatMessage("\n" + message);

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

		Log.d(CONSTANTS.TAG, "Lobby sendToPlayers l:" + socketMap.size());
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
		Log.d(CONSTANTS.TAG, "Player [" + player.name + "] disconnected from ["
				+ player.position + "]");
		socketMap.remove(player.position);
	}

	private void setBid(final String bid) {
		sendToHost(CONSTANTS.SOCKET_SendBid + bid);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				TextView bidView = (TextView) findViewById(R.id.game_bid_textview);
				bidView.setText(getString(R.string.game_bid_text, bid));
			}
		});
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
				setBid(bid);

			}

			break;
		default:
			break;
		}
	}

}
