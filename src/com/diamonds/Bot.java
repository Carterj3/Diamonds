package com.diamonds;

import java.util.ArrayList;
import java.util.Random;

import org.rosehulman.edu.carterj3.Card;
import org.rosehulman.edu.carterj3.Player;
import org.rosehulman.edu.carterj3.PlayerNotFoundException;

import android.util.Log;

public class Bot implements OnCommunication {

	public static Bot bot1 = null;
	public static Bot bot2 = null;
	public static Bot bot3 = null;

	NonHostSocket sock;
	private String username;

	private int position;

	private ArrayList<Card> hand;

	public Bot(String username, int position) {
		this.username = username;
		this.position = position;
	}

	public void StartBot() {
		sock = new NonHostSocket(this, "127.0.0.1", username);
		sock.start();
	}

	@Override
	public void onRecv(String msg, int id) {
		Log.d(MainActivity.tag, "Bot [" + username + "] :: " + msg);

		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_YourTurn)) {
			Random rng = org.rosehulman.edu.carterj3.CONSTANTS.getSeed();
			Card card = hand.get(rng.nextInt(hand.size()));
			sock.send(CONSTANTS.SOCKET_PlayCard+card);
		}
		if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_StartGame)) {
			sock.send(CONSTANTS.SOCKET_IsReady);
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendChat)) {
			String message = msg.split(":")[1];
			if (message.equals("bot")) {
				send("I am alive");
			}
		} else if (CONSTANTS.strncmp(msg, CONSTANTS.SOCKET_SendHand)) {
			send("Got a hand");

			this.hand = GameActivity.convertStringToHand(msg.split(":")[1]);

			sock.send(CONSTANTS.SOCKET_SendBid + (40 + position));
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
