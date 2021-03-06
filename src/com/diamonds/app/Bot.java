package com.diamonds.app;

import java.util.ArrayList;
import java.util.Random;

import org.rosehulman.edu.carterj3.Card;
import org.rosehulman.edu.carterj3.Player;
import org.rosehulman.edu.carterj3.PlayerNotFoundException;

import android.util.Log;

public class Bot implements OnCommunication {

	NonHostSocket sock;
	private String username;

	private ArrayList<Card> hand;

	public Bot(String username) {
		this.username = username;

	}

	public void StartBot() {
		sock = new NonHostSocket(this, "127.0.0.1", username);
		sock.start();
	}

	@Override
	public void onRecv(String msg, int id) {
		Log.d(CONSTANTS.TAG, "Bot [" + username + "] :: " + msg);

		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_YourTurn)) {
			if ((hand != null) && (hand.size() == 0)) {
				return;
			}

			Random rng = org.rosehulman.edu.carterj3.CONSTANTS.getSeed();
			Card card = hand.get(rng.nextInt(hand.size()));
			sock.send(CONSTANTS.SOCKET_PlayCard + card);
		}
		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_StartGame)) {
			sock.send(CONSTANTS.SOCKET_IsReady);
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendChat)) {
			String message = msg.split(":")[1];
			if (message.equals("bot")) {
				send("I am alive");
			}
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendHand)) {
			if (msg.split(":").length == 1) {
				this.hand = new ArrayList<Card>();
			} else {
				this.hand = GameActivity.convertStringToHand(msg.split(":")[1]);
			}

			sock.send(CONSTANTS.SOCKET_SendBid + 20);
		}
	}

	private void send(String msg) {
		String response = CONSTANTS.SOCKET_SendChat + msg + " [" + username
				+ "]";
		sock.send(response);
	}

	@Override
	public Player onConnection(SocketServerReplyThread newSocket, int id)
			throws PlayerNotFoundException {
		return null;
	}

	@Override
	public void onDisconnect(Player player) {

	}

}
